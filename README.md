# openJiuwen AgentStudio

openJiuwen AgentStudio提供了一站式AI Agent开发平台，为开发者提供从开发到部署的全栈解决方案。该部分采用低代码 / 零代码的可视化设计与编排工具，能让开发者快速打造和调试智能体和工作流。

---

## 1 文档适用人群

本文档适用于以下类型的读者：

| 适用人群 | 说明 |
|----------|------|
| **后端开发工程师** | 想要了解Java后端服务架构、API设计、业务逻辑实现的开发者 |
| **前端开发工程师** | 想要了解Angular前端项目结构、组件开发、路由配置的开发者 |
| **运维工程师** | 想要了解Docker部署、服务配置、环境变量设置的运维人员 |
| **技术架构师** | 想要了解整体系统架构、技术选型、模块划分的技术决策者 |
| **AI应用开发者** | 想要了解Agent开发平台能力、RAG知识库、工作流编排的开发者 |
| **测试工程师** | 想要了解系统功能模块、接口规范的测试人员 |

---

## 2 项目整体结构

```
agent-studio/
├── backend/                          # Java后端服务模块
├── frontend/                         # Angular前端应用模块
├── docs/                             # 项目文档模块
├── docker/                           # Docker部署配置
└── LICENSE / README.md 等根文件
```

---

## 3 backend 模块详解

backend模块是整个系统的核心后端服务，采用Maven多模块架构，包含API定义、业务逻辑、数据持久化等功能。

### 3.1 目录结构

```
backend/
├── pom.xml                           # 父POM文件，统一管理依赖版本
├── sql/                              # SQL脚本目录
│   ├── schema.sql                    # 数据库表结构定义
│   ├── init.sql                      # 初始化数据
│   └── data.sql                      # 业务测试数据
├── studio/                           # 聚合模块
├── studio-api/                       # API定义模块
├── studio-common/                    # 通用模块
├── studio-manager/                   # Manager服务启动模块
├── studio-manager-api/               # Manager API定义模块
├── studio-manager-service/           # Manager服务业务实现
├── studio-runtime/                   # Runtime服务启动模块
├── studio-runtime-api/               # Runtime API定义模块
├── studio-runtime-service/           # Runtime服务业务实现
├── studio-service/                   # 核心业务服务模块
└── studio-space/                     # DeepResearch
```

### 3.2 核心模块说明

#### 3.2.1 studio-common（通用模块）

**位置**：`backend/studio-common/`

**功能说明**：提供全项目通用的工具类、实体类和公共组件

**目录结构**：

```
studio-common/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/openjiuwen/studio/agent/common/
    │   │       ├── annotation/               # 自定义注解
    │   │       ├── bo/                       # 业务对象
    │   │       ├── config/                   # 配置类
    │   │       ├── constant/                 # 常量定义
    │   │       ├── crypt/                    # 加密工具
    │   │       ├── dto/                      # 数据传输对象
    │   │       ├── entity/                   # 实体类
    │   │       ├── enums/                    # 枚举定义
    │   │       ├── exception/                # 异常定义
    │   │       ├── filter/                   # 请求过滤器
    │   │       ├── rce/                      # 远程调用封装
    │   │       ├── redis/                    # Redis相关
    │   │       ├── sensitive/                # 敏感词处理
    │   │       ├── service/                  # 通用服务
    │   │       ├── utils/                    # 工具类
    │   │       └── validator/                # 数据校验
    │   └── resources/                        # 资源文件
    └── test/                                 # 测试代码
```

**各包功能详解**：

| 包名 | 功能说明 |
|------|----------|
| **annotation** | 存放自定义注解，用于权限控制、日志记录、方法拦截等场景 |
| **bo** | Business Object，业务对象，用于封装业务数据 |
| **config** | 配置类，管理各类中间件和第三方服务的连接配置 |
| **constant** | 常量定义，存放系统级常量配置 |
| **crypt** | 加密工具包，提供数据加密、解密、签名等安全功能 |
| **dto** | 数据传输对象，用于前后端数据交互 |
| **entity** | 实体类，与数据库表一一对应的ORM映射对象 |
| **enums** | 枚举定义，用于管理有限状态的对象集合 |
| **exception** | 自定义异常类，统一异常处理 |
| **filter** | HTTP请求过滤器，用于认证、日志、跨域等处理 |
| **rce** | 远程调用封装，提供Feign等远程调用工具 |
| **redis** | Redis相关操作封装 |
| **sensitive** | 敏感词过滤功能 |
| **service** | 通用业务服务 |
| **utils** | 通用工具类，提供字符串处理、日期转换、JSON序列化等工具方法 |
| **validator** | 数据校验注解和校验器 |

#### 3.2.2 studio-manager（Manager服务启动模块）

**位置**：`backend/studio-manager/`

**功能说明**：Agent管理服务，负责Agent的创建、配置、部署、监控等核心管理功能

**目录结构**：

```
studio-manager/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/openjiuwen/studio/agent/manager/
    │   │       └── Application.java          # Spring Boot启动类
    │   └── resources/                        # 资源文件
    └── test/                                 # 测试代码
```

**启动类功能**：

- 使用`@SpringBootApplication`标注
- 包扫描路径：`com.openjiuwen.studio.agent.manager`
- 提供Agent管理相关的REST API接口

#### 3.2.3 studio-runtime（Runtime服务启动模块）

**位置**：`backend/studio-runtime/`

**功能说明**：Agent运行时服务，负责Agent的实际执行、事件处理、监控等

**目录结构**：

```
studio-runtime/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/openjiuwen/studio/agent/runtime/
    │   │       └── Application.java          # Spring Boot启动类
    │   └── resources/                        # 资源文件
    └── test/                                 # 测试代码
```

**启动类功能**：

- 使用`@SpringBootApplication`标注
- 包扫描路径：`com.openjiuwen.studio.agent.runtime`
- 提供Agent运行时执行相关的REST API接口

#### 3.2.4 studio-manager-service（Manager服务业务实现）

**位置**：`backend/studio-manager-service/`

**功能说明**：Manager服务的核心业务逻辑实现模块，包含Agent管理、工作流编排、知识库管理等具体业务功能

**目录结构**：

```
studio-manager-service/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/openjiuwen/studio/
    │   │       ├── agent/
    │   │       │   ├── agentbase/            # Agent基础能力实现
    │   │       │   │   ├── client/           # RAG/知识库客户端集成
    │   │       │   │   ├── common/           # 通用常量和枚举
    │   │       │   │   ├── config/           # 配置类
    │   │       │   │   ├── converter/        # 数据转换器
    │   │       │   │   ├── entity/           # 实体类
    │   │       │   │   ├── enums/            # 枚举定义
    │   │       │   │   ├── filter/           # 请求过滤器
    │   │       │   │   ├── mapper/           # 数据库映射
    │   │       │   │   ├── model/            # 数据模型
    │   │       │   │   ├── service/          # 业务服务
    │   │       │   │   └── utils/            # 工具类
    │   │       │   ├── foundation/           # 基础能力
    │   │       │   │   ├── base/             # 基础实现
    │   │       │   │   ├── connection/       # 连接管理
    │   │       │   │   └── i18n/             # 国际化
    │   │       │   └── manager/              # 管理功能
    │   │       │       ├── aop/              # 切面编程
    │   │       │       ├── aspect/           # AspectJ切面
    │   │       │       ├── bo/               # 业务对象
    │   │       │       ├── config/           # 配置类
    │   │       │       ├── constant/         # 常量定义
    │   │       │       ├── controller/       # REST控制器
    │   │       │       ├── dao/              # 数据访问对象
    │   │       │       ├── dto/              # 数据传输对象
    │   │       │       ├── entity/           # 实体类
    │   │       │       ├── enums/            # 枚举定义
    │   │       │       ├── exception/        # 异常定义
    │   │       │       ├── filter/           # 过滤器
    │   │       │       ├── http/             # HTTP客户端
    │   │       │       ├── license/          # 许可证管理
    │   │       │       ├── mapper/           # MyBatis映射
    │   │       │       ├── obs/              # OBS对象存储
    │   │       │       ├── rce/              # 远程调用
    │   │       │       ├── repository/       # 仓库层
    │   │       │       ├── ros/              # ROS集成
    │   │       │       ├── saml/             # SAML认证
    │   │       │       ├── service/          # 业务服务
    │   │       │       ├── task/             # 任务调度
    │   │       │       ├── utils/            # 工具类
    │   │       │       └── workflow/         # 工作流管理
    │   │       ├── common/
    │   │       │   └── service/              # 公共服务
    │   │       └── prompt/
    │   │           └── engineering/          # 提示词工程
    │   └── resources/                        # 资源文件
    └── test/                                 # 测试代码
```

**各包功能详解**：

| 包名 | 功能说明 |
|------|----------|
| **agent/agentbase/client** | 集成RAG服务、知识库SDK等外部客户端 |
| **agent/agentbase/entity** | Agent基础能力相关的实体类 |
| **agent/agentbase/service** | Agent基础能力业务服务 |
| **agent/manager/controller** | Agent管理相关的REST API控制器 |
| **agent/manager/service** | Agent管理业务逻辑实现 |
| **agent/manager/workflow** | 工作流编排和管理功能 |
| **agent/manager/dao/mapper** | MyBatis数据库映射接口 |
| **agent/manager/obs** | 华为云OBS对象存储集成 |
| **agent/manager/license** | 软件许可证管理 |
| **common/service** | 各模块共用的公共服务 |
| **prompt/engineering** | 提示词生成、优化、测试功能 |

#### 3.2.5 studio-runtime-service（Runtime服务业务实现）

**位置**：`backend/studio-runtime-service/`

**功能说明**：Runtime服务的核心业务逻辑实现模块，负责Agent的实际执行、事件处理、运行时监控等

**目录结构**：

```
studio-runtime-service/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/openjiuwen/studio/agent/runtime/
    │   │       ├── alarm/                      # 告警模块
    │   │       │   ├── aom/                    # AOM告警集成
    │   │       │   └── app/                    # 应用告警
    │   │       ├── annotation/                 # 自定义注解
    │   │       ├── aop/                        # 切面编程
    │   │       ├── bo/                         # 业务对象
    │   │       ├── config/                     # 配置类
    │   │       ├── constant/                   # 常量定义
    │   │       ├── controller/                 # REST控制器
    │   │       ├── datasource/                 # 数据源管理
    │   │       ├── dto/                        # 数据传输对象
    │   │       ├── entity/                     # 实体类
    │   │       ├── enums/                      # 枚举定义
    │   │       ├── event/                      # 事件处理
    │   │       ├── exception/                  # 异常定义
    │   │       ├── filter/                     # 请求过滤器
    │   │       ├── http/                       # HTTP客户端
    │   │       ├── mapper/                     # 数据库映射
    │   │       ├── mcp/                        # MCP协议支持
    │   │       ├── model/                      # 数据模型
    │   │       ├── properties/                 # 配置属性
    │   │       ├── rce/                        # 远程调用
    │   │       ├── redis/                      # Redis缓存
    │   │       ├── sensitive/                  # 敏感词过滤
    │   │       ├── service/                    # 业务服务
    │   │       ├── task/                       # 任务调度
    │   │       ├── thread/                     # 线程管理
    │   │       └── utils/                      # 工具类
    │   └── resources/                          # 资源文件
    └── test/                                   # 测试代码
```

**各包功能详解**：

| 包名 | 功能说明 |
|------|----------|
| **alarm/aom** | 华为云应用运维管理（AOM）告警集成，实现运行时告警上报 |
| **alarm/app** | 应用层告警服务，提供应用级告警能力 |
| **aop** | 面向切面编程，用于日志记录、性能监控、事务管理等 |
| **controller** | Runtime服务REST API控制器，暴露执行接口 |
| **datasource** | 多数据源管理，支持运行时数据源切换和连接池管理 |
| **event** | 事件处理机制，支持Agent运行过程中的事件订阅和分发 |
| **mcp** | Model Context Protocol协议支持，实现与MCP服务器的通信 |
| **service** | Agent执行相关的核心业务服务 |
| **thread** | 线程池管理，控制并发执行 |
| **task** | 定时任务调度，管理周期性任务 |
| **sensitive** | 敏感词过滤，内容安全检查 |
| **dto/entity** | 数据传输对象和实体类定义 |

#### 3.2.6 studio-space（DeepResearch）

**位置**：`backend/studio-space/`

**功能说明**：DeepResearch

**目录结构**：

```
studio-space/
├── pom.xml
├── studio-space-api/                # API定义
├── studio-space-app/                # 应用层
├── studio-space-common/             # 通用组件
├── studio-space-dao/                # 数据访问层
├── studio-space-foundation/         # 基础能力
└── studio-space-service/            # 业务服务
```

**各子模块功能说明**：

| 模块名 | 功能说明 |
|--------|----------|
| **studio-space-api** | DeepResearch的API定义，包括工作空间创建、查询、成员管理等接口 |
| **studio-space-app** | 应用层实现，处理业务编排和流程控制 |
| **studio-space-common** | DeepResearch通用组件，供其他子模块复用 |
| **studio-space-dao** | 数据访问层，负责与数据库交互 |
| **studio-space-foundation** | 基础能力层，提供工作空间管理的核心抽象和实现 |
| **studio-space-service** | 业务服务层，实现工作空间的具体的业务逻辑 |

### 3.3 SQL脚本说明

| 文件名 | 功能说明 |
|--------|----------|
| **schema.sql** | 数据库表结构定义，包含所有业务表的DDL语句 |
| **init.sql** | 初始化数据，包括系统配置、字典表等基础数据 |
| **data.sql** | 业务数据，存放初始业务测试数据 |

### 3.4 核心数据库表说明

系统使用关系型数据库存储业务数据，基于Java实体类逆向推断，以下是核心数据表的结构和关系说明。

#### 3.4.1 用户相关表

**t_users（用户表）**

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键，自增 |
| username | VARCHAR(100) | 用户名，唯一 |
| real_name | VARCHAR(100) | 真实姓名 |
| email | VARCHAR(100) | 邮箱 |
| phone | VARCHAR(20) | 电话 |
| department | VARCHAR(100) | 部门 |
| position | VARCHAR(100) | 职位 |
| source | VARCHAR(20) | 用户来源（INTERNAL/SAML/OAUTH/LDAP） |
| domain_id | VARCHAR(100) | 租户ID |
| project_id | VARCHAR(100) | 项目ID |
| is_active | BOOLEAN | 是否激活 |
| created_time | DATETIME | 创建时间 |
| updated_time | DATETIME | 更新时间 |
| expire_time | DATETIME | 过期时间 |

#### 3.4.2 工作空间相关表

**WorkspaceEntity（工作空间表）**

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | VARCHAR | 主键，工作空间ID |
| name | VARCHAR | 工作空间名称 |
| flag | VARCHAR | 工作空间标识 |
| project_id | VARCHAR | 项目ID |
| description | VARCHAR | 描述 |
| icon | VARCHAR | 图标 |
| tenant_id | VARCHAR | 租户ID |
| type | VARCHAR | 类型 |
| status | VARCHAR | 状态 |
| is_preset_agent | INT | 是否预制过教学模版 |
| created_on | DATETIME | 创建时间 |
| creator | VARCHAR | 创建人 |
| creator_id | VARCHAR | 创建人ID |
| updated_on | DATETIME | 更新时间 |
| updater | VARCHAR | 更新人 |
| updater_id | VARCHAR | 更新人ID |
| role | VARCHAR | 空间角色 |

**WorkSpaceMemberEntity（工作空间成员表）**

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | VARCHAR | 主键 |
| workspace_id | VARCHAR | 工作空间ID，外键 |
| member_id | VARCHAR | 成员ID |
| member_name | VARCHAR | 成员名称 |
| member_source | VARCHAR | 成员来源 |
| domain_id | VARCHAR | 租户ID |
| role | VARCHAR | 角色 |
| status | INT | 状态 |
| created_on | DATETIME | 创建时间 |
| creator_id | VARCHAR | 创建人ID |

**表关系**：`WorkSpaceMemberEntity.workspace_id` -> `WorkspaceEntity.id`

#### 3.4.3 Agent相关表

**AgentBaseInfo（Agent基础信息表）**

| 字段名 | 类型 | 说明 |
|--------|------|------|
| agent_id | VARCHAR | 主键，Agent唯一标识 |
| domain_id | VARCHAR | 租户ID |
| project_id | VARCHAR | 项目ID |
| workspace_id | VARCHAR | 工作空间ID |
| name | VARCHAR | Agent名称 |
| status | VARCHAR | 状态 |
| tenant_id | VARCHAR | 租户ID |
| created_on | DATETIME | 创建时间 |

**AgentVersion（Agent版本表）**

| 字段名 | 类型 | 说明 |
|--------|------|------|
| version_id | VARCHAR | 主键，版本唯一标识 |
| agent_id | VARCHAR | Agent ID，外键 |
| project_id | VARCHAR | 项目ID |
| name | VARCHAR | 版本名称 |
| description | VARCHAR | 描述 |
| tags | JSON | 标签列表 |
| icon | VARCHAR | 图标 |
| instructions | TEXT | 系统指令 |
| prologue | TEXT | 开场白 |
| suggest_queries | JSON | 推荐问题 |
| trigger_list | JSON | 触发器配置 |
| additional_questions_config | JSON | 追问配置 |
| ir_path | VARCHAR | IR路径 |
| dsl_path | VARCHAR | DSL路径 |
| is_online | BOOLEAN | 是否上线 |
| creator | VARCHAR | 创建者 |
| created_on | DATETIME | 创建时间 |
| updated_on | DATETIME | 更新时间 |
| published_on | DATETIME | 发布时间 |

**表关系**：`AgentVersion.agent_id` -> `AgentBaseInfo.agent_id`

#### 3.4.4 工作流相关表

**WorkflowEntity（工作流表）**

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | VARCHAR | 主键，工作流ID |
| name | VARCHAR | 名称 |
| code | VARCHAR | 编码 |
| description | VARCHAR | 描述 |
| avatar | VARCHAR | 头像 |
| icon_name | VARCHAR | 图标名称 |
| dsl_path | VARCHAR | DSL文件路径 |
| ir_path | VARCHAR | IR文件路径 |
| status | VARCHAR | 状态 |
| visibility | VARCHAR | 可见性 |
| workflow_type | VARCHAR | 工作流类型 |
| customize_node | INT | 是否自定义节点 |
| created_at | BIGINT | 创建时间戳 |
| updated_at | BIGINT | 更新时间戳 |
| published_at | BIGINT | 发布时间戳 |
| created_by | VARCHAR | 创建人 |
| creator_id | VARCHAR | 创建人ID |
| updated_by | VARCHAR | 更新人 |
| updater_id | VARCHAR | 更新人ID |
| project_id | VARCHAR | 项目ID |
| domain_id | VARCHAR | 租户ID |
| workspace_id | VARCHAR | 工作空间ID |
| deploy_wf_version | BIGINT | 部署版本号 |
| last_version_id | VARCHAR | 最新版本ID |
| test_status | INT | 测试状态 |
| is_share | INT | 是否共享 |
| share_info | JSON | 共享信息 |
| trigger_list | JSON | 触发器列表 |

**WorkflowVersionEntity（工作流版本表）**

| 字段名 | 类型 | 说明 |
|--------|------|------|
| version_id | VARCHAR | 主键，版本唯一标识 |
| version_name | VARCHAR | 版本名称 |
| （继承 WorkflowEntity 全部字段） | | |

**表关系**：`WorkflowVersionEntity.version_id` -> `WorkflowEntity.last_version_id`

**SessionEntity（工作流会话表）**

| 字段名 | 类型 | 说明 |
|--------|------|------|
| session_id | VARCHAR | 主键，会话ID |
| instance_id | VARCHAR | 实例ID |
| workflow_id | VARCHAR | 工作流ID，外键 |
| project_id | VARCHAR | 项目ID |
| created_on | DATETIME | 创建时间 |
| updated_on | DATETIME | 更新时间 |

**表关系**：`SessionEntity.workflow_id` -> `WorkflowEntity.id`

#### 3.4.5 知识库相关表

**KnowledgeBaseEntity（知识库表）**

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | VARCHAR | 主键，知识库ID |
| name | VARCHAR | 名称 |
| type | VARCHAR | 类型 |
| status | VARCHAR | 状态 |
| icon | VARCHAR | 图标 |
| knowledge_base_connection_id | VARCHAR | 知识库连接ID |
| connector_id | VARCHAR | 连接器ID |
| external_id | VARCHAR | 外部ID |
| description | VARCHAR | 描述 |
| repo_type | VARCHAR | 仓库类型 |
| create_time | BIGINT | 创建时间 |
| update_time | BIGINT | 更新时间 |
| project_id | VARCHAR | 项目ID |
| domain_id | VARCHAR | 租户ID |
| domain_name | VARCHAR | 租户名 |
| workspace_id | VARCHAR | 工作空间ID |
| created_user_id | VARCHAR | 创建人ID |
| created_user_name | VARCHAR | 创建人名称 |
| last_update_user_id | VARCHAR | 更新人ID |
| last_update_user_name | VARCHAR | 更新人名称 |
| copy_source_id | VARCHAR | 复制来源ID |
| share_scope | VARCHAR | 共享范围 |

**KnowledgeBaseConnectionEntity（知识库连接配置表）**

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | VARCHAR | 主键 |
| name | VARCHAR | 连接名称 |
| type | VARCHAR | 连接类型 |
| config | TEXT | 连接配置 |
| project_id | VARCHAR | 项目ID |
| domain_id | VARCHAR | 租户ID |
| created_time | BIGINT | 创建时间 |

#### 3.4.6 MCP服务相关表

**McpServerEntity（MCP服务器模板表）**

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | VARCHAR | 主键，UUID |
| server_code | VARCHAR | MCP标识 |
| name | VARCHAR | 中文名 |
| name_en | VARCHAR | 英文名 |
| icon | VARCHAR | 图标 |
| description | VARCHAR | 中文描述 |
| description_en | VARCHAR | 英文描述 |
| readme | TEXT | Markdown文档 |
| url | VARCHAR | 官方地址 |
| server_config | TEXT | 安装配置 |
| tools | JSON | 工具集 |
| type | VARCHAR | 内置/个人 |
| org_type | VARCHAR | 安装类型（NPX/UVX/SSE） |
| category | VARCHAR | 类别 |
| view_times | BIGINT | 查看次数 |
| install_times | BIGINT | 安装次数 |

**McpServiceEntity（MCP服务实例表）**

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | VARCHAR | 主键，UUID |
| name | VARCHAR | 服务名称 |
| name_en | VARCHAR | 英文名称 |
| description | VARCHAR | 描述 |
| readme | TEXT | Markdown文档 |
| org_type | VARCHAR | 拉包类型（NPX/UVX/SSE） |
| deploy_type | VARCHAR | 部署类型（SSE/stdio/streamable_http） |
| server_config | TEXT | 服务配置 |
| tools | JSON | 工具列表 |
| fc_instance_url | VARCHAR | 函数实例URL |
| fc_instance_id | VARCHAR | 函数实例ID |
| fc_instance_status | VARCHAR | 函数实例状态 |
| function_name | VARCHAR | 函数名称 |
| server_id | VARCHAR | 服务模板ID，外键 |
| project_id | VARCHAR | 项目ID |
| workspace_id | VARCHAR | 工作空间ID |
| domain_id | VARCHAR | 租户ID |
| icon | VARCHAR | 图标 |
| visibility | VARCHAR | 可见性 |
| is_share | INT | 是否被共享 |
| origin | INT | 来源 |
| created_date | TIMESTAMP | 创建时间 |
| last_updated_date | TIMESTAMP | 更新时间 |

**表关系**：`McpServiceEntity.server_id` -> `McpServerEntity.id`

#### 3.4.7 应用相关表

**App（应用表）**

| 字段名 | 类型 | 说明 |
|--------|------|------|
| app_id | VARCHAR | 主键，应用ID |
| project_id | VARCHAR | 项目ID |
| workspace_id | VARCHAR | 工作空间ID |
| name | VARCHAR | 应用名称 |
| description | VARCHAR | 描述 |
| icon | VARCHAR | 图标 |
| icon_name | VARCHAR | 图标名称 |
| tags | JSON | 标签列表 |
| app_type | VARCHAR | 应用类型（chat/scene） |
| resource_id | VARCHAR | 资源ID（agent或workflow的ID） |
| resource_type | VARCHAR | 资源类型（agent/workflow） |
| workflow_type | VARCHAR | 工作流类型 |
| input_params | TEXT | 输入参数 |
| output_params | TEXT | 输出参数 |
| prologue | TEXT | 开场白 |
| suggest_queries | JSON | 推荐问题 |
| creator | VARCHAR | 创建者 |
| published_on | DATETIME | 发布时间 |

**表关系**：
- `App.resource_id` -> `AgentBaseInfo.agent_id`（当resource_type='agent'时）
- `App.resource_id` -> `WorkflowEntity.id`（当resource_type='workflow'时）

#### 3.4.8 数据源相关表

**DatasourceEntity（数据源表）**

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | VARCHAR | 主键，UUID |
| project_id | VARCHAR | 项目ID |
| workspace_id | VARCHAR | 工作空间ID |
| domain_id | VARCHAR | 租户ID |
| name | VARCHAR | 数据源名称 |
| type | VARCHAR | 类型（MYSQL等） |
| desc | VARCHAR | 描述 |
| internet_access | VARCHAR | 接入网络类型 |
| region | VARCHAR | Region |
| instance_id | VARCHAR | RDS实例ID |
| instance_name | VARCHAR | 实例名称 |
| connection_info | TEXT | 连接信息（JSON格式） |
| status | VARCHAR | 状态（success/failed） |
| last_error_message | TEXT | 最后错误信息 |
| created_by | VARCHAR | 创建人 |
| creator_id | VARCHAR | 创建人ID |
| created_on | DATETIME | 创建时间 |
| updated_by | VARCHAR | 更新人 |
| updater_id | VARCHAR | 更新人ID |
| updated_on | DATETIME | 更新时间 |

#### 3.4.9 技能相关表

**SkillEntity（技能表）**

| 字段名 | 类型 | 说明 |
|--------|------|------|
| skill_id | VARCHAR | 主键，技能ID |
| domain_id | VARCHAR | 租户ID |
| name | VARCHAR | 名称 |
| icon | VARCHAR | 图标 |
| status | VARCHAR | 状态 |
| source | VARCHAR | 来源 |
| description | VARCHAR | 描述 |
| creator_id | VARCHAR | 创建人ID |
| creator_name | VARCHAR | 创建人名称 |
| latest_version | VARCHAR | 最新版本 |
| used_version | VARCHAR | 使用版本 |
| created_at | BIGINT | 创建时间 |
| updated_at | BIGINT | 更新时间 |
| workspace_id | VARCHAR | 工作空间ID |
| project_id | VARCHAR | 项目ID |
| published_asset | INT | 是否已发布 |

**SkillVersionEntity（技能版本表）**

| 字段名 | 类型 | 说明 |
|--------|------|------|
| version_id | VARCHAR | 主键，版本ID |
| skill_id | VARCHAR | 技能ID，外键 |
| version | VARCHAR | 版本号 |
| config | TEXT | 版本配置 |
| created_at | BIGINT | 创建时间 |

**表关系**：`SkillVersionEntity.skill_id` -> `SkillEntity.skill_id`

#### 3.4.10 API密钥相关表

**ApiKeysEntity（API密钥表）**

| 字段名 | 类型 | 说明 |
|--------|------|------|
| api_key_id | VARCHAR | 主键，密钥ID |
| api_key_name | VARCHAR | 密钥名称 |
| api_key_value | VARCHAR | 密钥值 |
| description | VARCHAR | 描述 |
| project_id | VARCHAR | 项目ID |
| workspace_id | VARCHAR | 工作空间ID |
| domain_id | VARCHAR | 租户ID |
| user_id | VARCHAR | 用户ID |
| user_name | VARCHAR | 用户名 |
| created_by_user_name | VARCHAR | 创建人 |
| last_updated_by_user_name | VARCHAR | 最近使用人 |
| created_date | BIGINT | 创建时间 |

#### 3.4.11 表关系总览

```
t_users (用户表)
    │
    ├── WorkSpaceMemberEntity (工作空间成员) ──── WorkspaceEntity (工作空间)
    │                                            │
    │                                            ├── AgentBaseInfo (Agent基础信息)
    │                                            │       │
    │                                            │       └── AgentVersion (Agent版本)
    │                                            │
    │                                            ├── WorkflowEntity (工作流)
    │                                            │       │
    │                                            │       ├── WorkflowVersionEntity (工作流版本)
    │                                            │       │
    │                                            │       └── SessionEntity (会话)
    │                                            │
    │                                            ├── App (应用)
    │                                            │       │
    │                                            │       └── (关联Agent或Workflow)
    │                                            │
    │                                            ├── DatasourceEntity (数据源)
    │                                            │
    │                                            └── SkillEntity (技能)
    │                                                    │
    │                                                    └── SkillVersionEntity (技能版本)
    │
    ├── McpServerEntity (MCP服务器模板)
    │       │
    │       └── McpServiceEntity (MCP服务实例)
    │
    └── ApiKeysEntity (API密钥)
```

#### 3.4.12 核心外键关联说明

| 父表 | 子表 | 关联字段 | 说明 |
|------|------|----------|------|
| WorkspaceEntity | WorkSpaceMemberEntity | workspace_id | 工作空间与成员一对多 |
| WorkspaceEntity | AgentBaseInfo | workspace_id | 工作空间与Agent一对多 |
| WorkspaceEntity | WorkflowEntity | workspace_id | 工作空间与工作流一对多 |
| WorkspaceEntity | App | workspace_id | 工作空间与应用一对多 |
| AgentBaseInfo | AgentVersion | agent_id | Agent与版本一对多 |
| WorkflowEntity | WorkflowVersionEntity | id->last_version_id | 工作流与版本一对多 |
| WorkflowEntity | SessionEntity | workflow_id | 工作流与会话一对多 |
| McpServerEntity | McpServiceEntity | id->server_id | MCP模板与服务实例一对多 |
| SkillEntity | SkillVersionEntity | skill_id | 技能与版本一对多 |
| t_users | WorkSpaceMemberEntity | member_id | 用户与工作空间成员关联 |
| t_users | ApiKeysEntity | user_id | 用户与API密钥关联 |

---

## 4 frontend 模块详解

frontend模块是项目的前端部分，基于Angular框架构建，提供用户交互界面。

### 4.1 目录结构

```
frontend/
├── angular.json                        # Angular项目配置文件
├── package.json                        # NPM依赖配置文件
├── tsconfig.json                       # TypeScript配置
├── tailwind.config.js                  # Tailwind CSS配置
├── eslint.config.mjs                   # ESLint代码检查配置
├── .nvmrc                              # Node版本指定
├── README.md                           # 前端说明文档
├── src/                                # 源代码目录
│   ├── index.html                      # HTML入口文件
│   ├── main.ts                         # Angular应用入口
│   ├── app/                            # 应用根组件
│   │   ├── app.component.ts
│   │   ├── app.component.html
│   │   ├── app.component.less
│   │   └── index.ts
│   ├── agentcore/                      # Agent核心模块
│   ├── core/                           # 核心模块
│   │   ├── i18n/                       # 国际化
│   │   ├── providers/                  # 依赖注入提供者
│   │   └── services/                   # 核心服务
│   ├── shared/                         # 共享模块
│   │   ├── base/                       # 基础组件
│   │   ├── components/                 # 共享组件
│   │   ├── config/                     # 共享配置
│   │   ├── decorators/                 # 装饰器
│   │   ├── directives/                 # 指令
│   │   ├── guard/                      # 路由守卫
│   │   ├── services/                   # 共享服务
│   │   └── validation/                 # 校验
│   ├── routes/                         # 路由模块
│   │   ├── agent-center/               # Agent中心
│   │   ├── app-center/                 # 应用中心
│   │   ├── code-editor/                # 代码编辑器
│   │   ├── datasource-management/      # 数据源管理
│   │   ├── development-configuration/  # 开发配置
│   │   ├── experience-creation/        # 体验创建
│   │   ├── health/                     # 健康检查
│   │   ├── home/                       # 首页
│   │   ├── information-template/       # 信息模板
│   │   ├── intent-package/             # 意图包
│   │   ├── knowledge-center/           # 知识中心
│   │   ├── left-menu/                  # 左侧菜单
│   │   ├── memory-lib/                 # 记忆库
│   │   ├── mobile/                     # 移动端
│   │   ├── model-management/           # 模型管理
│   │   ├── model-square/               # 模型广场
│   │   ├── overview/                   # 概览
│   │   ├── platform-management/        # 平台管理
│   │   ├── plugin-market/              # 插件市场
│   │   ├── prompt/                     # 提示词
│   │   ├── service-market/             # 服务市场
│   │   ├── subscription/               # 订阅
│   │   ├── tool/                       # 工具
│   │   ├── web-page-experience/        # 网页体验
│   │   └── root.routes.ts              # 根路由配置
│   ├── services/                       # 全局服务
│   ├── constants/                      # 常量定义
│   ├── enums/                          # 枚举定义
│   ├── models/                         # 数据模型
│   ├── interfaces/                     # 接口定义
│   ├── pipes/                          # 管道
│   ├── utils/                          # 工具函数
│   ├── assets/                         # 静态资源
│   ├── styles/                         # 全局样式
│   ├── environment/                    # 环境配置
│   ├── mock/                           # Mock数据
│   ├── classes/                        # 类型 class
│   ├── types/                          # 类型定义
│   └── single-spa/                     # 微前端配置
└── .husky/                             # Git钩子配置
```

### 4.2 前端技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| **核心框架** | Angular | 20.3.17 |
| **UI组件库** | NG-ZORRO (Ant Design for Angular) | 20.4.4 |
| **状态管理** | RxJS | 7.8.0 |
| **图表库** | ECharts | 5.6.0 |
| **图编辑** | AntV X6 | 3.1.4 |
| **Markdown** | ngx-markdown | 20.1.0 |
| **代码编辑器** | Monaco Editor | 0.52.2 |
| **国际化** | i18next | 22.4.10 |
| **CSS框架** | Tailwind CSS | 3.2.7 |
| **构建工具** | Angular Build / Webpack | 20.3.13 / 5.95.0 |
| **语言** | TypeScript | 5.8.3 |

### 4.3 核心功能模块说明

| 模块路径 | 功能说明 |
|----------|----------|
| **routes/agent-center** | Agent中心，提供Agent的创建、配置、管理的可视化界面 |
| **routes/app-center** | 应用中心，管理Agent应用的生命周期 |
| **routes/knowledge-center** | 知识中心，提供知识库的创建、上传、检索功能 |
| **routes/plugin-market** | 插件市场，浏览、安装、管理插件 |
| **routes/prompt** | 提示词管理，提供提示词的编写、调试、优化功能 |
| **routes/model-management** | 模型管理，配置和管理大语言模型 |
| **routes/datasource-management** | 数据源管理，管理各种外部数据源连接 |
| **routes/development-configuration** | 开发配置，提供开发环境和参数配置 |
| **routes/memory-lib** | 记忆库，管理Agent的记忆配置 |
| **routes/code-editor** | 代码编辑器，提供在线编写代码的能力 |
| **shared/components** | 通用UI组件库，供各模块复用 |
| **core/services** | 核心服务，包含API调用、认证、全局状态管理等 |

---

## 5 docs 模块详解

docs模块存放项目的各类技术文档，为开发者提供详细的使用和开发指南。

### 5.1 目录结构

```
docs/
├── images/                             # 文档图片资源
│   └── (各种截图和示意图)
├── API参考.md                           # API接口参考文档
├── 安装部署指南.md                       # 安装部署指南
├── LICENSE                             # 许可协议文档
├── 项目架构.md                           # 项目架构说明文档
└── 用户指南.md                           # 用户使用指南
```

### 5.2 文档功能说明

| 文件名 | 功能说明 |
|--------|----------|
| **API参考.md** | API接口参考文档，详细描述所有REST API的请求/响应格式、参数说明、错误码等 |
| **安装部署指南.md** | 详细的安装部署步骤，包括环境准备、Docker部署配置、数据库初始化等 |
| **项目架构.md** | 项目整体架构说明，包括技术选型、模块划分、系统设计等 |
| **用户指南.md** | 用户使用指南，详细介绍平台各项功能的使用方法 |

---

## 6 docker 模块详解

docker模块提供项目的Docker容器化部署配置。

### 6.1 目录结构

```
docker/
├── compose/                            # Docker Compose配置
├── k8s/                                # Kubernetes配置
├── studio-console/                     # 控制台Docker配置
├── studio-manager/                     # Manager服务Docker配置
├── studio-runtime/                     # Runtime服务Docker配置
├── studio-service/                     # 通用服务Docker配置
├── build.sh                            # Docker镜像构建脚本
├── package.sh                          # 打包脚本
├── init.sql                            # Docker环境初始化SQL
├── README.md                           # Docker部署说明
└── .gitattributes                      # Git属性配置
```

### 6.2 部署相关脚本说明

| 文件/目录 | 功能说明 |
|-----------|----------|
| **build.sh** | Docker镜像构建脚本，负责构建各个服务的Docker镜像 |
| **package.sh** | 打包脚本，负责将应用打包成可部署的格式 |
| **compose/** | Docker Compose编排文件，定义多容器部署配置 |
| **studio-manager/** | Manager服务的Docker配置和环境变量 |
| **studio-runtime/** | Runtime服务的Docker配置和环境变量 |

---

## 7 项目技术栈汇总

### 7.1 后端技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| **核心框架** | Spring Boot | 3.5.14 |
| **安全框架** | Spring Security | 6.5.10 |
| **Java版本** | JDK | 17 |
| **ORM框架** | Hibernate | 6.6.8.Final |
| **数据库** | PostgreSQL | 42.7.11 |
| **缓存** | Redis (Redisson) | 3.39.0 |
| **数据库** | H2 | 2.2.224 |
| **数据库** | OpenGauss JDBC | 5.0.0 |
| **对象存储** | OBS SDK | 3.23.9 |
| **通信框架** | Netty | 4.1.133.Final |
| **HTTP客户端** | OkHttp SSE | 4.12.0 |
| **HTTP客户端** | HTTPClient5 | 5.4.4 |
| **对象映射** | MapStruct | 1.6.3 |
| **JSON处理** | FastJSON2 | 2.0.51 |
| **认证** | JWT (Nimbus) | 10.3 |
| **任务调度** | Quartz | 2.5.2 |
| **文档处理** | Apache POI | 5.4.1 |
| **MCP协议** | MCP SDK | 0.16.0 |

### 7.2 前端技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| **核心框架** | Angular | 20.3.17 |
| **UI组件库** | NG-ZORRO | 20.4.4 |
| **状态管理** | RxJS | 7.8.0 |
| **图表库** | ECharts | 5.6.0 |
| **图编辑** | AntV X6 | 3.1.4 |
| **Markdown** | ngx-markdown | 20.1.0 |
| **代码编辑器** | Monaco Editor | 0.52.2 |
| **国际化** | i18next | 22.4.10 |
| **CSS框架** | Tailwind CSS | 3.2.7 |
| **构建工具** | Angular Build | 20.3.13 |
| **语言** | TypeScript | 5.8.3 |

---

## 8 项目架构图

```
┌─────────────────────────────────────────────────────────────────────┐
│                               Frontend                              │
│  Agent中心 | 知识中心 | 插件市场 | 提示词管理 | 模型管理 | 工作流编排    │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      Studio Agent Manager                            │
│  Agent管理 | 工作流管理 | 知识库管理 | 插件管理 | 模型服务管理          │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      Studio Agent Runtime                            │
│  Agent执行引擎 | 事件处理 | AOP监控 | MCP支持 | 数据源管理              │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      Studio Prompt Engineering                       │
│  提示词生成 | 提示词优化 | 提示词测试 | 版本管理                        │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│                         Studio Common                                │
│  通用组件 | 工具类 | 实体类 | 常量枚举 | 过滤器 | 加密工具               │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│                            Data Layer                                │
│                   MySQL | Redis | OBS | H2 (开发环境)                │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 9 快速开始

### 9.1 环境要求

| 要求       | 版本      |
|----------|---------|
| JDK      | 17+     |
| Maven    | 3.8+    |
| Node.js  | 18+     |
| npm/pnpm | 9+ / 7+ |
| MySQL    | 8+      |
| Redis    | 6+      |
| Docker   | 20+     |

### 9.2 后端开发

1. 克隆代码仓库
2. 执行`backend/sql/schema.sql`创建数据库表
3. 执行`backend/sql/init.sql`和`data.sql`初始化数据
4. 配置数据库和Redis连接信息
5. 运行Maven构建：`mvn clean install`
6. 启动Manager服务或Runtime服务

### 9.3 前端开发

1. 进入`frontend`目录
2. 安装依赖：`pnpm install`
3. 启动开发服务器：`pnpm start`

### 9.4 Docker部署

1. 源码编译构建指导：[源码编译构建指导.md](docker/%E6%BA%90%E7%A0%81%E7%BC%96%E8%AF%91%E6%9E%84%E5%BB%BA%E6%8C%87%E5%AF%BC.md)
2. 安装部署指南：[安装部署指南.md](docs/%E5%AE%89%E8%A3%85%E9%83%A8%E7%BD%B2%E6%8C%87%E5%8D%97.md)

---

## 10 更多信息

- 安装部署参考 [安装部署指南](docs/安装部署指南.md)
- 快速体验参考 [用户指南](docs/用户指南.md)
- API接口参考 [API参考](docs/API参考.md)
- 架构设计参考 [项目架构](docs/项目架构.md)

---

# ⚖️ **许可证**

本项目采用 Apache 2.0 许可证。详见 [LICENSE](LICENSE) 文件。

# 🤝 **贡献指南**

欢迎提交 Issue 和 Pull Request！
