"""Minimal OBS SDK compatibility layer for local tests."""


class _DummyBody:
    def __init__(self, buffer=b""):
        self.buffer = buffer


class _DummyResponse:
    def __init__(self, status=200, body=None):
        self.status = status
        self.body = body or _DummyBody()


class ObsClient:
    def __init__(self, *args, **kwargs):
        pass

    def headBucket(self, bucket_name):
        return _DummyResponse(status=200)

    def putContent(self, bucket_name, object_key, content):
        return _DummyResponse(status=200)

    def getObject(self, bucketName=None, objectKey=None, loadStreamInMemory=True):
        return _DummyResponse(status=404)

    def copyObject(
        self, source_bucket_name, source_object_key, dest_bucket_name, dest_object_key
    ):
        return _DummyResponse(status=200)

    def deleteObject(self, bucket_name, object_key):
        return _DummyResponse(status=204)

    def createSignedUrl(self, method=None, bucketName=None, objectKey=None):
        return {
            "signedUrl": "",
            "actualSignedRequestHeaders": {},
        }
