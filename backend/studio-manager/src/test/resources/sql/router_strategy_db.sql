/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

INSERT
INTO t_router_strategy (ID, STRATEGY_NAME, STRATEGY_TYPE, STRATEGY_KEY, STRATEGY_TAGS, STRATEGY_DESCRIPTION, SERVICE_ID_LIST, STRATEGY_TIMEOUT, STRATEGY_RETRY_COUNT, SERVICE_COUNT, PREPARE_ATTRIBUTE, DOMAIN_ID, PROJECT_ID, WORKSPACE_ID, CREATED_BY_USER_NAME, LAST_UPDATED_BY_USER_NAME, CREATED_DATE, LAST_UPDATED_DATE,TRACE_ID)
VALUES ('test_model_router', 'TestModelRouter', 'default', 'strategy:default:eewe', NULL, 'TAG', '64c8e9bb-7ded-4fc9-b63e-9ff3f0e7a588', 1000, 0, 1, NULL, 'test_domain_id', 'test_project_id', 'default', '2', '2', 1754917935896, 1754917935896,'test_model_router');