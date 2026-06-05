import json
import os

import urllib3
from app.common.logger import LOG
from jiuwen.common.security.cryptor import Crypt


class AlarmHandler:
    @staticmethod
    def handle(event):
        location_info = event.location_info
        location_info["recourceId"] = event.resource_id
        location_info["recourceName"] = event.resource_name
        try:
            alarm_input_dto = {
                "alarm_id": event.event_id,
                "category": event.category.value,
                "location_info": location_info,
                "additional_info": event.additional,
                "cause": event.cause,
            }
            body = json.dumps({"alarms": [alarm_input_dto]})
            project_id = event.user_info.get("project_id")
            tls_cert_key_passwd = Crypt().decrypt(os.environ.get("TLS_CERT_KEY_PASSWD"))
            http = urllib3.PoolManager(
                cert_file=os.environ.get("TLS_CERT_PATH"),
                key_file=os.environ.get("TLS_CERT_KEY_PATH"),
                key_password=tls_cert_key_passwd,
                cert_reqs="CERT_NONE",
            )
            url = (
                "https://"
                + os.environ.get("omservice_endpoint")
                + "/v1/"
                + project_id
                + "/itep/omservice/alarms"
            )
            headers = {"Content-Type": "application/json"}
            rsp = http.request("POST", url, body=body, headers=headers)
            if 201 == rsp.status or 200 == rsp.status:
                LOG.info("send alarm successfully: %s", event.event_id)
            else:
                LOG.error("send alarm failed: %s", rsp.data)
        except Exception as e:
            LOG.error("send alarm error: %s", e)
