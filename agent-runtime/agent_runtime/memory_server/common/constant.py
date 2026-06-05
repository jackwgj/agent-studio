from enum import Enum


class DeploymentMode(Enum):
    HC = ("hc",)
    HCS = ("hcs",)
    # lite环境
    SIMPLE = "simple"

    @classmethod
    def get_deployment_mode(cls, deployment_mode: str):
        normalized_value = deployment_mode.strip().lower()

        for item in cls:
            if item.value == normalized_value:
                return item

        return DeploymentMode.HC
