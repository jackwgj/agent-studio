import {McpInterfaces, McpMonitorData, McpTool} from "@interfaces/mcp/mcp.interfaces";
import {MCP_DEFAULT_ICON_BASE64_STR} from "@shared/config/default-icons-base64";

const mcpToolData = (): McpTool[] => {
  return [
    {
      "name": "createVideoMeeting",
      "description": "meeting",
      "description_en": "create video meeting",
      "inputSchema": {
        "type": "object",
        "properties": {
          "hostUserId": {
              "type": "string",
              "description": "userId",
              description_en: "host user id",
              default: "terminal",
              properties: null,
              value: '',
              copyValue: '',
              isRequired: false,
              isRight: false,
          },
          "attendeesIdList": {
            "type": "array",
            "items": {
              "type": "string"
            },
            default: '',
            properties: null,
            "description": "id_list",
            "description_en": "attendees id list",
            value: '',
            copyValue: '',
            isRequired: false,
            isRight: false,
          },
          "duration": {
            "type": "number",
            "default": 30,
            "description": "30",
            properties: null,
            description_en: 'duration',
            value: '',
              copyValue: '',
            isRequired: false,
            isRight: false,
          },
          "cycleParams": {
            "type": "object",
            "properties": {
              "startDate": {
                "type": "string",
                "description": "data_desc",
                description_en: 'start date',
                default: '',
                properties: null,
                value: '',
                  copyValue: '',
                isRequired: false,
                isRight: false,
              },
              "endDate": {
                "type": "string",
                default: '',
                properties: null,
                "description": "date_desc",
                description_en: 'end date',
                value: '',
                isRequired: false,
                isRight: false,
                  copyValue: '',
              },
              "cycle": {
                "type": "string",
                "description": "date",
                default: '',
                properties: null,
                description_en: 'cycle',
                value: '',
                isRequired: false,
                isRight: false,
                  copyValue: '',
              },
              "interval": {
                "type": "number",
                "default": 1,
                "description": "",
                properties: null,
                description_en: 'interval',
                value: '',
                isRequired: false,
                isRight: false,
                  copyValue: '',
              },
              "point": {
                "type": "array",
                "items": {
                  "type": "number",
                },
                "description": "",
                description_en: 'point',
                properties: null,
                default: '',
                value: '',
                isRequired: false,
                isRight: false,
                  copyValue: ''
              },
              "preRemindDays": {
                "type": "number",
                "default": 1,
                "description": "",
                description_en: 'pre remind days',
                properties: null,
                value: '',
                isRequired: false,
                isRight: false,
                  copyValue: '',
              }
            },
            "required": [
              "startDate",
              "endDate",
              "cycle",
              "point"
            ],
            "additionalProperties": false,
            "default": null,
            "description": "",
            description_en: 'cycle params',
            value: '',
            isRequired: false,
            isRight: false,
              copyValue: '',
          }
        },
        "required": [
          "hostUserId"
        ],
        "additionalProperties": false
      },
      "isCollapsed": true,
    },
    {
      "name": "cancelVideoMeeting",
      "description": "",
      "description_en": "cancel video meeting",
      "inputSchema": {
        "type": "object",
        "properties": {
          "hostUserId": {
            "type": "string",
            "description": "",
            default: '',
            properties: null,
            description_en: "host user id",
            value: '',
            isRequired: false,
            isRight: false,
              copyValue: '',
          },
          "attendeesIdList": {
            "type": "array",
            "items": {
              "type": "number"
            },
            default: '',
            properties: null,
            "description": "",
            description_en: "attendees id list",
            value: '',
            isRequired: false,
            isRight: false,
              copyValue: '',
          },
          "duration": {
            "type": "number",
            "default": 30,
            "description": "",
            properties: null,
            description_en: 'duration',
            value: '',
            isRequired: false,
            isRight: false,
              copyValue: '',
          },
        },
        "required": [
          "hostUserId"
        ],
        "additionalProperties": false
      },
      "isCollapsed": true,
    }
  ]
};

const mcpMonitorData = (): McpMonitorData => {
  return {
    oneDay: 12,
    sevenDay: 24,
    thirtyDay: 36,
    sum: [
      {
        sum: 30,
        date: "07-25",
      },
      {
        sum: 24,
        date: "07-26",
      },
      {
        sum: 15,
        date: "07-28"
      }
    ],
    average: [
      {
        average: 50,
        date: "07-25"
      },
      {
        average: 40,
        date: "07-26"
      },
      {
        average: 15,
        date: "07-28"
      }
    ]
  }
}

export const mockMcpList: Array<McpInterfaces> = [
    {
        id: "1",
        name: "name",
        name_en: "Empty Template",
        description: "Base on empty template to create mcp service",
        description_en: "Base on empty template to create mcp service",
        deploy_type: "sse",
        fc_instance_status: "success",
        status_cn: "installed",
        status_en: "installed",
        install_times: 50,
        view_times: 0,
        created_on: "",
        creator: "terminal",
        icon: MCP_DEFAULT_ICON_BASE64_STR,
        desc: "Base on empty template to create mcp service",
        config_status: "done",
        type: "inner",
        type_cn: "inner",
        type_en: "platform",
        isHover: false,
        created_date: "2025-07-26T04:22:37.000+00:00",
        last_updated_date: "2025-07-26T08:22:37.000+00:00",
        readme: "",
        tools: mcpToolData(),
        isSelected: true,
        url: "",
        org_type: "SSE",
        server_config: "",
        monitorData: mcpMonitorData(),
        score: 2,
    },
    {
        id: "83f8d7898a6d49b3bac8f77d56112727",
        name: "B4",
        name_en: "Meeting",
        description: "",
        description_en: "",
        deploy_type: "sse",
        fc_instance_status: "success",
        status_cn: "",
        status_en: "installed",
        install_times: 10,
        view_times: 0,
        created_on: "",
        creator: "Terminal",
        icon: "data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNDguMDAwMDAwIiBoZWlnaHQ9IjQ4LjAwMDAwMCIgdmlld0JveD0iMCAwIDQ4IDQ4IiBmaWxsPSJub25lIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHhtbG5zOnhsaW5rPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5L3hsaW5rIj4KCTxkZXNjPgoJCQlDcmVhdGVkIHdpdGggUGl4c28uCgk8L2Rlc2M+Cgk8ZGVmcz4KCQk8Y2xpcFBhdGggaWQ9ImNsaXAxXzU0OCI+CgkJCTxyZWN0IHdpZHRoPSI0OC4wMDAwMDAiIGhlaWdodD0iNDguMDAwMDAwIiBmaWxsPSJ3aGl0ZSIgZmlsbC1vcGFjaXR5PSIwIi8+CgkJPC9jbGlwUGF0aD4KCTwvZGVmcz4KCTxnIGNsaXAtcGF0aD0idXJsKCNjbGlwMV81NDgpIj4KCQk8cmVjdCByeD0iOC4wMDAwMDAiIHdpZHRoPSI0Ny45OTk5OTIiIGhlaWdodD0iNDcuOTk5OTkyIiBmaWxsPSIjMEJCOEIyIiBmaWxsLW9wYWNpdHk9IjEuMDAwMDAwIi8+CgkJPHBhdGggZD0iTTQwIDIwTDQwIDMzLjU2QzQwIDM1LjA3IDM4LjUzIDM2IDM3IDM2TDEwLjcyIDM2QzkuMTkgMzYgOCAzNS4wNyA4IDMzLjU2TDggMjBMNDAgMjBaTTE5LjUzIDEyTDIyLjI2IDE0LjdMMzcuMjYgMTQuN0MzOC44IDE0LjcgNDAgMTUuODkgNDAgMTcuNEw0MCAxOEw4IDE4TDggMTQuN0M4IDEzLjI1IDkuMDIgMTIuMDYgMTAuNDkgMTJMMTAuNzIgMTJMMTkuNTMgMTJaIiBmaWxsPSIjRkZGRkZGIiBmaWxsLW9wYWNpdHk9IjEuMDAwMDAwIiBmaWxsLXJ1bGU9ImV2ZW5vZGQiLz4KCTwvZz4KPC9zdmc+Cg==",
        desc: "",
        config_status: "done",
        type: "inner",
        type_cn: "",
        type_en: "platform",
        isHover: false,
        created_date: "2025-07-25T01:22:37.000+00:00",
        last_updated_date: "2025-07-25T08:22:37.000+00:00",
        readme: '',
        tools: mcpToolData(),
        isSelected: false,
        url: "",
        org_type: "SSE",
        server_config: "",
        monitorData: mcpMonitorData(),
        score: 2,
    },
    {
        id: "82f8d7888a6d49b3bac8777d56112727",
        name: "",
        name_en: "Ticket",
        description: "",
        description_en: "",
        deploy_type: "sse",
        fc_instance_status: "creatingStack",
        status_cn: "",
        status_en: "installed",
        install_times: 18,
        view_times: 0,
        created_on: "",
        creator: "Terminal",
        icon: "data:image/png;base64,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",
        desc: "",
        config_status: "done",
        type: "user",
        type_cn: "",
        type_en: "user",
        isHover: false,
        created_date: "2025-07-26T01:22:37.000+00:00",
        last_updated_date: "2025-07-26T03:22:37.000+00:00",
        readme: '',
        tools: mcpToolData(),
        isSelected: false,
        url: "",
        org_type: "SSE",
        server_config: "",
        monitorData: mcpMonitorData(),
        score: 2,
    }
]
