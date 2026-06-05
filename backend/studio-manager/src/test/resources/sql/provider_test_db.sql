/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */
DELETE FROM t_user_model_service_provider WHERE ID = 'agentBuilder';
INSERT  INTO t_user_model_service_provider (ID, PROVIDER_NAME, PROVIDER_NAME_EN, DESCRIPTION, TAGS, PROVIDER_URL, CREATED_DATE, LAST_UPDATED_DATE, LOGO, STATUS, DOMAIN_ID, PROJECT_ID, WORKSPACE_ID, CREATED_BY_USER, LAST_UPDATED_BY_USER, IDENTITY_ID) VALUES ('agentBuilder', 'agentBuilder', 'agentBuilder', '', NULL, '', 1754668644437, 1754668644437, 'data:image=', 'created', '0', 'test_project_id', 'default', '2', '2', 'dc1a26b2-a2c4-4086-81fa-b0de8160fbac');

DELETE FROM t_user_model_service_provider WHERE ID = 'test1';
INSERT  INTO t_user_model_service_provider (ID, PROVIDER_NAME, PROVIDER_NAME_EN, DESCRIPTION, TAGS, PROVIDER_URL, CREATED_DATE, LAST_UPDATED_DATE, LOGO, STATUS, DOMAIN_ID, PROJECT_ID, WORKSPACE_ID, CREATED_BY_USER, LAST_UPDATED_BY_USER, IDENTITY_ID) VALUES ('test1', '盘古', 'agentBuilder', '', NULL, '', 1754668644437, 1754668644437, 'data:image=', 'created', '0', 'test_project_id', 'default', '2', '2', 'dc1a26b2-a2c4-4086-81fa-b0de8160fbac');

DELETE FROM t_user_model_service_provider WHERE ID = 'test2';
INSERT  INTO t_user_model_service_provider (ID, PROVIDER_NAME, PROVIDER_NAME_EN, DESCRIPTION, TAGS, PROVIDER_URL, CREATED_DATE, LAST_UPDATED_DATE, LOGO, STATUS, DOMAIN_ID, PROJECT_ID, WORKSPACE_ID, CREATED_BY_USER, LAST_UPDATED_BY_USER, IDENTITY_ID) VALUES ('test2', '盘古', 'agentBuilder', '', NULL, '', 1754668644437, 1754668644437, 'data:image=', 'created', '0', 'test_project_id', 'default', '2', '2', 'dc1a26b2-a2c4-4086-81fa-b0de8160fbac');

DELETE FROM t_provider_auth_metadata WHERE ID = 'agentBuilder-mm';
INSERT  INTO t_provider_auth_metadata (ID, PROVIDER_ID, AUTH_TYPE, AUTH_INFO, AUTH_URL, CREATED_BY_USER, CREATED_DATE, LAST_UPDATED_DATE, DOMAIN_ID, PROJECT_ID, WORKSPACE_ID, IDENTITY_ID) VALUES ('agentBuilder-mm', 'agentBuilder', 'API_KEY', '{\"API Key\":\"\"}', '', '2', 1754668644437, 1754668644437, '0', 'test_project_id', 'default', '070ad379-92e9-4063-a4ac-87010aaa89f6');

DELETE FROM t_sys_model_service_provider WHERE ID = '100';
INSERT  INTO t_sys_model_service_provider (ID, PROVIDER_NAME, PROVIDER_NAME_EN, DESCRIPTION, TAGS, PROVIDER_URL, CREATED_DATE, LAST_UPDATED_DATE, LOGO, STATUS, priority) VALUES ('100', 'ModelArts Studio', 'ModelArts Studio', 'ModelArts Standard是面向AI开发者的一站式开发平台，提供了简洁易用的管理控制台，包含数据管理、开发环境、模型训练、模型管理、部署上线等端到端的AI开发工具链。', '中文,英文,文本对话,图像理解', '', 1753675200000, 1753675200000, 'data', 'created', 0);

DELETE FROM t_provider_auth_metadata WHERE ID = 'ab-mm';
INSERT  INTO t_provider_auth_metadata (ID, PROVIDER_ID, AUTH_TYPE, AUTH_INFO, AUTH_URL, CREATED_BY_USER, CREATED_DATE, LAST_UPDATED_DATE, DOMAIN_ID, PROJECT_ID, WORKSPACE_ID, IDENTITY_ID) VALUES ('ab-mm', 'ab', 'API_KEY', '{\"API Key\":\"\"}', '', '2', 1754668644437, 1754668644437, '0', 'test_project_id', 'default', '070ad379-92e9-4063-a4ac-87010aaa89f6');


DELETE FROM t_user_model_service_provider WHERE ID = 'ab';
INSERT  INTO t_user_model_service_provider (ID, PROVIDER_NAME, PROVIDER_NAME_EN, DESCRIPTION, TAGS, PROVIDER_URL, CREATED_DATE, LAST_UPDATED_DATE, LOGO, STATUS, DOMAIN_ID, PROJECT_ID, WORKSPACE_ID, CREATED_BY_USER, LAST_UPDATED_BY_USER, IDENTITY_ID) VALUES ('ab', 'ab', 'ab', '', NULL, '', 1754668644437, 1754668644437, 'data:image=', 'created', '0', 'test_project_id', 'default', '2', '2', 'dc1a26b2-a2c4-4086-81fa-b0de8160fbac');
