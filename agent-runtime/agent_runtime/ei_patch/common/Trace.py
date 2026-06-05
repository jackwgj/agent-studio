import json
import os

import urllib3
from app.common.logger import LOG
from flask import g
from flask import request
from jiuwen.common.security.cryptor import Crypt


def send_trace(audit_entity):
    source_id = request.remote_addr
    user = {
        "domain": {"name": g.user_info.get("domain_name"), "id": g.domain_id},
        "name": g.user_info.get("user_name"),
        "id": g.user_id,
    }
    try:
        trace_input = {
            "time": int(audit_entity.time),
            "user": json.dumps(user),
            "service_type": "PLM",
            "resource_type": audit_entity.resource_type.value,
            "resource_name": audit_entity.resource_name,
            "source_ip": source_id,
            "resource_id": audit_entity.resource_id,
            "trace_name": audit_entity.trace_name,
            "trace_rating": audit_entity.trace_rating.value,
            "trace_type": audit_entity.trace_type.value,
            "request": json.dumps(audit_entity.request),
            "response": json.dumps(audit_entity.response),
            "code": int(audit_entity.code),
            "message": json.dumps(audit_entity.message),
            "location_info": audit_entity.location_info,
        }
        if audit_entity.api_version:
            trace_input["api_version"] = audit_entity.api_version

        if audit_entity.request_id:
            trace_input["request_id"] = audit_entity.request_id

        body = json.dumps({"traces": [trace_input]})
        tls_cert_key_passwd = Crypt().decrypt(os.environ.get("TLS_CERT_KEY_PASSWD"))
        http = urllib3.PoolManager(
            cert_file=os.environ.get("TLS_CERT_PATH"),
            key_file=os.environ.get("TLS_CERT_KEY_PATH"),
            key_password=tls_cert_key_passwd,
            cert_reqs="CERT_NONE",
        )
        project_id = g.project_id
        url = (
            "https://"
            + os.environ.get("omservice_endpoint")
            + "/v1/"
            + project_id
            + "/itep/omservice/traces"
        )
        headers = {"Content-Type": "application/json"}
        timeout = urllib3.Timeout(total=15)
        rsp = http.request("POST", url, body=body, headers=headers, timeout=timeout)
        if 201 == rsp.status or 200 == rsp.status:
            LOG.info("send trace successfully: %s", audit_entity.trace_name)
        else:
            LOG.error("send trace failed: %s", rsp.data)
    except Exception as e:
        LOG.error("send trace error: %s", e)
