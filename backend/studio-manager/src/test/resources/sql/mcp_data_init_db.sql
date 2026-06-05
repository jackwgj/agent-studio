DELETE
FROM ws_mcp_server_def
WHERE ID = '914';
INSERT INTO ws_mcp_server_def (id, server_code, icon, name, name_en, description, description_en, readme, server_config,
                               tools, type, org_type, deleted, tenant_id, dept_code, created_date, created_by_user_id,
                               last_updated_date, last_updated_by_user_id, url, install_times, view_times)
VALUES ('914', 'Hyperbrowser', '', '超浏览器AI自动化', null,
        '欢迎来到Hyperbrowser，这是专为人工智能打造的互联网平台。Hyperbrowser是下一代平台，它赋予人工智能代理强大功能，并实现简单易行且可扩展的浏览器自动化操作。该平台专为AI开发者设计，消除了本地基础设施带来的烦恼和性能瓶颈，让您能够…',
        null, '# Hyperbrowser MCP 服务器
[![smithery 徽章](https://smithery.ai/badge/@hyperbrowserai/mcp)](https://smithery.ai/server/@hyperbrowserai/mcp)

![Frame 5](https://github.com/user-attachments/assets/3309a367-e94b-418a-a047-1bf1ad549c0a)

这是 Hyperbrowser 的模型上下文协议 (MCP) 服务器。它提供了各种工具来抓取、提取结构化数据和爬取网页。它还提供了对通用浏览器代理（如 OpenAI 的 CUA、Anthropic 的 Claude 计算机使用和浏览器使用）的轻松访问。

有关 Hyperbrowser 的更多信息，请参见 [这里](https://docs.hyperbrowser.ai/)。Hyperbrowser API 支持 mcp 服务器中功能的一个超集。

关于 Model Context Protocol 的更多信息，请参见 [这里](https://modelcontextprotocol.io/introduction)。

## 目录

- [安装](#installation)
- [使用](#usage)
- [工具](#tools)
- [配置](#configuration)
- [许可证](#license)

## 安装

### 手动安装
要安装服务器，请运行：

```bash
npx hyperbrowser-mcp <YOUR-HYPERBROWSER-API-KEY>
```


## 在 Cursor 上运行
将以下内容添加到 `~/.cursor/mcp.json` 中：
```json
{
  mcpServers: {
    hyperbrowser: {
      command: npx,
      args: [-y, hyperbrowser-mcp],
      env: {
        HYPERBROWSER_API_KEY: YOUR-API-KEY
      }
    }
  }
}
```


## 在 Windsurf 上运行
将以下内容添加到你的 `./codeium/windsurf/model_config.json` 中：
```json
{
  mcpServers: {
    hyperbrowser: {
      command: npx,
      args: [-y, hyperbrowser-mcp],
      env: {
        HYPERBROWSER_API_KEY: YOUR-API-KEY
      }
    }
  }
}
```


### 开发

出于开发目的，你可以直接从源代码运行服务器。

1. 克隆仓库：

   ```sh
   git clone git@github.com:hyperbrowserai/mcp.git hyperbrowser-mcp
   cd hyperbrowser-mcp
   ```

2. 安装依赖项：

   ```sh
   npm install # 或 yarn install
   npm run build
   ```

3. 运行服务器：

   ```sh
   node dist/server.js
   ```

## Claude 桌面应用程序
这是一个用于 Claude 桌面客户端的 Hyperbrowser MCP 服务器示例配置。

```json
{
  mcpServers: {
    hyperbrowser: {
      command: npx,
      args: [--yes, hyperbrowser-mcp],
      env: {
        HYPERBROWSER_API_KEY: your-api-key
      }
    }
  }
}
```



## 工具
* `scrape_webpage` - 从任何网页中提取格式化的内容（例如 markdown、截图等）
* `crawl_webpages` - 遍历多个链接页面并提取适合 LLM 的格式化内容
* `extract_structured_data` - 将混乱的 HTML 转换为结构化的 JSON
* `search_with_bing` - 使用 Bing 搜索查询网络并获取结果
* `browser_use_agent` - 使用 Browser Use 代理进行快速轻量级的浏览器自动化
* `openai_computer_use_agent` - 使用 OpenAI 的 CUA 模型进行通用自动化
* `claude_computer_use_agent` - 使用 Claude 计算机使用进行复杂的浏览器任务

### 通过 Smithery 安装

要通过 [Smithery](https://smithery.ai/server/@hyperbrowserai/mcp) 自动为 Claude 桌面安装 Hyperbrowser MCP 服务器：

```bash
npx -y @smithery/cli install @hyperbrowserai/mcp --client claude
```


## 资源

服务器通过 `resources` 方法提供关于 hyperbrowser 的文档。任何可以进行资源发现的客户端都可以访问这些文档。

## 许可证

本项目采用 MIT 许可证。', '{
  "mcpServers": {
    "hyperbrowser": {
      "command": "npx",
      "args": ["-y", "hyperbrowser-mcp"],
      "env": {
        "HYPERBROWSER_API_KEY": "YOUR-API-KEY"
      }
    }
  }
}', null, 'inner', 'NPX', 0, 'system', 'system', '2025-04-22 11:13:17', 'system', '2025-04-22 11:13:24', 'system', 'https://github.com/hyperbrowserai/mcp', 0, 21);
DELETE
FROM ws_mcp_server_def
WHERE ID = '915';
INSERT INTO ws_mcp_server_def (id, server_code, icon, name, name_en, description, description_en, readme, server_config,
                               tools, type, org_type, deleted, tenant_id, dept_code, created_date, created_by_user_id,
                               last_updated_date, last_updated_by_user_id, url, install_times, view_times)
VALUES ('915', 'PerplexityAsk', '', 'Perplexity-Ask', null,
        '一种MCP服务器实现，它集成了Sonar API，为克劳德提供了无与伦比的实时、全网研究能力。', null, '# Perplexity Ask MCP 服务器

一个集成了 Sonar API 的 MCP 服务器实现，为 Claude 提供无与伦比的实时全网研究功能。

![演示](perplexity-ask/assets/demo_screenshot.png)


## 工具

- **perplexity_ask**
  - 通过与 Sonar API 进行对话来执行实时网络搜索。
  - **输入:**
    - `messages` (数组): 会话消息数组。
      - 每条消息必须包含：
        - `role` (字符串): 消息的角色（例如，`system`, `user`, `assistant`）。
        - `content` (字符串): 消息的内容。

## 配置

### 第一步：

克隆此仓库：

```bash
git clone git@github.com:ppl-ai/modelcontextprotocol.git
```


导航到 `perplexity-ask` 目录并安装必要的依赖项：

```bash
cd modelcontextprotocol/perplexity-ask && npm install
```


### 第二步：获取 Sonar API 密钥

1. 注册 [Sonar API 账户](https://docs.perplexity.ai/guides/getting-started)。
2. 按照账户设置说明从开发者仪表板生成您的 API 密钥。
3. 在环境变量中将 API 密钥设置为 `PERPLEXITY_API_KEY`。

### 第三步：配置 Claude 桌面版

1. 在[这里](https://claude.ai/download)下载 Claude 桌面版。

2. 将以下内容添加到您的 `claude_desktop_config.json` 文件中：

```json
{
  mcpServers: {
    perplexity-ask: {
      command: docker,
      args: [
        run,
        -i,
        --rm,
        -e,
        PERPLEXITY_API_KEY,
        mcp/perplexity-ask
      ],
      env: {
        PERPLEXITY_API_KEY: YOUR_API_KEY_HERE
      }
    }
  }
}
```


### NPX

```json
{
  mcpServers: {
    perplexity-ask: {
      command: npx,
      args: [
        -y,
        server-perplexity-ask
      ],
      env: {
        PERPLEXITY_API_KEY: YOUR_API_KEY_HERE
      }
    }
  }
}
```


您可以使用以下命令访问文件：

```bash
vim ~/Library/Application\\ Support/Claude/claude_desktop_config.json
```


### 第四步：构建 Docker 镜像

Docker 构建命令：

```bash
docker build -t mcp/perplexity-ask:latest -f Dockerfile .
```


### 第五步：测试

让我们确保 Claude 桌面版已经识别我们在 `perplexity-ask` 服务器中暴露的两个工具。您可以通过查找锤子图标来确认这一点：

![Claude 可视化工具](perplexity-ask/assets/visual-indicator-mcp-tools.png)

点击锤子图标后，您应该能看到 Filesystem MCP 服务器提供的工具：

![可用集成](perplexity-ask/assets/available_tools.png)

如果您看到这两个工具，这意味着集成已激活。恭喜！这意味着 Claude 现在可以使用 Perplexity 了。然后您可以像使用 Perplexity 网页应用程序一样使用它。

### 第六步：高级参数

目前使用的搜索参数是默认值。您可以在 `index.ts` 脚本中直接修改 API 调用中的任何搜索参数。为此，请参考官方 [API 文档](https://docs.perplexity.ai/api-reference/chat-completions)。

### 故障排除

Claude 文档提供了一个非常优秀的 [故障排除指南](https://modelcontextprotocol.io/docs/tools/debugging)，您可以参考。但如果您需要额外的支持或 [报告错误](https://github.com/ppl-ai/api-discussion/issues)，仍然可以通过 api@perplexity.ai 联系我们。


## 许可证

此 MCP 服务器根据 MIT 许可证发布。这意味着您可以自由地使用、修改和分发该软件，但需遵守 MIT 许可证的条款和条件。更多详情，请参见项目仓库中的 LICENSE 文件。', '{
  "mcpServers": {
    "perplexity-ask": {
      "command": "npx",
      "args": [
        "-y",
        "server-perplexity-ask"
      ],
      "env": {
        "PERPLEXITY_API_KEY": "YOUR_API_KEY_HERE"
      }
    }
  }
}', null, 'inner', 'NPX', 0, 'system', 'system', '2025-04-22 11:13:16', 'system', '2025-04-22 11:13:24', 'system', 'https://github.com/ppl-ai/modelcontextprotocol', 0, 20);
DELETE
FROM ws_mcp_server_def
WHERE ID = '917';
INSERT INTO ws_mcp_server_def (id, server_code, icon, name, name_en, description, description_en, readme, server_config,
                               tools, type, org_type, deleted, tenant_id, dept_code, created_date, created_by_user_id,
                               last_updated_date, last_updated_by_user_id, url, install_times, view_times)
VALUES ('917', 'GitHub', '', 'GitHub', null, '用于GitHub API的MCP服务器，支持文件操作、仓库管理、搜索功能等更多功能。',
        null, '# GitHub MCP 服务器

用于 GitHub API 的 MCP 服务器，支持文件操作、仓库管理、搜索功能等。

### 功能

- **自动分支创建**：在创建/更新文件或推送更改时，如果分支不存在则会自动创建
- **全面的错误处理**：针对常见问题提供清晰的错误信息
- **Git 历史记录保留**：操作过程中保持 Git 历史记录，不会强制推送
- **批量操作**：支持单个文件和多个文件的操作
- **高级搜索**：支持代码、问题/PR 和用户的搜索

## 工具

1. `create_or_update_file`
   - Create or update a single file in a repository
   - Inputs:
     - `owner` (string): Repository owner (username or organization)
     - `repo` (string): Repository name
     - `path` (string): Path where to create/update the file
     - `content` (string): Content of the file
     - `message` (string): Commit message
     - `branch` (string): Branch to create/update the file in
     - `sha` (optional string): SHA of file being replaced (for updates)
   - Returns: File content and commit details

2. `push_files`
   - Push multiple files in a single commit
   - Inputs:
     - `owner` (string): Repository owner
     - `repo` (string): Repository name
     - `branch` (string): Branch to push to
     - `files` (array): Files to push, each with `path` and `content`
     - `message` (string): Commit message
   - Returns: Updated branch reference

3. `search_repositories`
   - Search for GitHub repositories
   - Inputs:
     - `query` (string): Search query
     - `page` (optional number): Page number for pagination
     - `perPage` (optional number): Results per page (max 100)
   - Returns: Repository search results

4. `create_repository`
   - Create a new GitHub repository
   - Inputs:
     - `name` (string): Repository name
     - `description` (optional string): Repository description
     - `private` (optional boolean): Whether repo should be private
     - `autoInit` (optional boolean): Initialize with README
   - Returns: Created repository details

5. `get_file_contents`
   - Get contents of a file or directory
   - Inputs:
     - `owner` (string): Repository owner
     - `repo` (string): Repository name
     - `path` (string): Path to file/directory
     - `branch` (optional string): Branch to get contents from
   - Returns: File/directory contents

6. `create_issue`
   - Create a new issue
   - Inputs:
     - `owner` (string): Repository owner
     - `repo` (string): Repository name
     - `title` (string): Issue title
     - `body` (optional string): Issue description
     - `assignees` (optional string[]): Usernames to assign
     - `labels` (optional string[]): Labels to add
     - `milestone` (optional number): Milestone number
   - Returns: Created issue details

7. `create_pull_request`
   - Create a new pull request
   - Inputs:
     - `owner` (string): Repository owner
     - `repo` (string): Repository name
     - `title` (string): PR title
     - `body` (optional string): PR description
     - `head` (string): Branch containing changes
     - `base` (string): Branch to merge into
     - `draft` (optional boolean): Create as draft PR
     - `maintainer_can_modify` (optional boolean): Allow maintainer edits
   - Returns: Created pull request details

8. `fork_repository`
   - Fork a repository
   - Inputs:
     - `owner` (string): Repository owner
     - `repo` (string): Repository name
     - `organization` (optional string): Organization to fork to
   - Returns: Forked repository details

9. `create_branch`
   - Create a new branch
   - Inputs:
     - `owner` (string): Repository owner
     - `repo` (string): Repository name
     - `branch` (string): Name for new branch
     - `from_branch` (optional string): Source branch (defaults to repo default)
   - Returns: Created branch reference

10. `list_issues`
    - List and filter repository issues
    - Inputs:
      - `owner` (string): Repository owner
      - `repo` (string): Repository name
      - `state` (optional string): Filter by state (''open'', ''closed'', ''all'')
      - `labels` (optional string[]): Filter by labels
      - `sort` (optional string): Sort by (''created'', ''updated'', ''comments'')
      - `direction` (optional string): Sort direction (''asc'', ''desc'')
      - `since` (optional string): Filter by date (ISO 8601 timestamp)
      - `page` (optional number): Page number
      - `per_page` (optional number): Results per page
    - Returns: Array of issue details

11. `update_issue`
    - Update an existing issue
    - Inputs:
      - `owner` (string): Repository owner
      - `repo` (string): Repository name
      - `issue_number` (number): Issue number to update
      - `title` (optional string): New title
      - `body` (optional string): New description
      - `state` (optional string): New state (''open'' or ''closed'')
      - `labels` (optional string[]): New labels
      - `assignees` (optional string[]): New assignees
      - `milestone` (optional number): New milestone number
    - Returns: Updated issue details

12. `add_issue_comment`
    - Add a comment to an issue
    - Inputs:
      - `owner` (string): Repository owner
      - `repo` (string): Repository name
      - `issue_number` (number): Issue number to comment on
      - `body` (string): Comment text
    - Returns: Created comment details

13. `search_code`
    - Search for code across GitHub repositories
    - Inputs:
      - `q` (string): Search query using GitHub code search syntax
      - `sort` (optional string): Sort field (''indexed'' only)
      - `order` (optional string): Sort order (''asc'' or ''desc'')
      - `per_page` (optional number): Results per page (max 100)
      - `page` (optional number): Page number
    - Returns: Code search results with repository context

14. `search_issues`
    - Search for issues and pull requests
    - Inputs:
      - `q` (string): Search query using GitHub issues search syntax
      - `sort` (optional string): Sort field (comments, reactions, created, etc.)
      - `order` (optional string): Sort order (''asc'' or ''desc'')
      - `per_page` (optional number): Results per page (max 100)
      - `page` (optional number): Page number
    - Returns: Issue and pull request search results

15. `search_users`
    - Search for GitHub users
    - Inputs:
      - `q` (string): Search query using GitHub users search syntax
      - `sort` (optional string): Sort field (followers, repositories, joined)
      - `order` (optional string): Sort order (''asc'' or ''desc'')
      - `per_page` (optional number): Results per page (max 100)
      - `page` (optional number): Page number
    - Returns: User search results

16. `list_commits`


   - 获取仓库中某个分支的提交记录
   - 输入参数：
     - `owner` (字符串): 仓库所有者
     - `repo` (字符串): 仓库名称
     - `page` (可选字符串): 页码
     - `per_page` (可选字符串): 每页的记录数
     - `sha` (可选字符串): 分支名称
   - 返回: 提交记录列表

17. `get_issue`
   - 获取仓库内特定问题的内容
   - 输入参数：
     - `owner` (字符串): 仓库所有者
     - `repo` (字符串): 仓库名称
     - `issue_number` (数字): 要检索的问题编号
   - 返回: Github 问题对象及详情

18. `get_pull_request`
   - 获取特定拉取请求的详细信息
   - 输入参数：
     - `owner` (字符串): 仓库所有者
     - `repo` (字符串): 仓库名称
     - `pull_number` (数字): 拉取请求编号
   - 返回: 包括差异和审查状态在内的拉取请求详细信息

19. `list_pull_requests`
   - 列出并过滤仓库的拉取请求
   - 输入参数：
     - `owner` (字符串): 仓库所有者
     - `repo` (字符串): 仓库名称
     - `state` (可选字符串): 按状态过滤 (''open'', ''closed'', ''all'')
     - `head` (可选字符串): 按头用户/组织及其分支过滤
     - `base` (可选字符串): 按基础分支过滤
     - `sort` (可选字符串): 排序依据 (''created'', ''updated'', ''popularity'', ''long-running'')
     - `direction` (可选字符串): 排序方向 (''asc'', ''desc'')
     - `per_page` (可选数字): 每页的结果数量（最大100）
     - `page` (可选数字): 页码
   - 返回: 拉取请求详细信息数组

20. `create_pull_request_review`
   - 在拉取请求上创建审查
   - 输入参数：
     - `owner` (字符串): 仓库所有者
     - `repo` (字符串): 仓库名称
     - `pull_number` (数字): 拉取请求编号
     - `body` (字符串): 审查评论文本
     - `event` (字符串): 审查动作 (''APPROVE'', ''REQUEST_CHANGES'', ''COMMENT'')
     - `commit_id` (可选字符串): 要审查的提交SHA
     - `comments` (可选数组): 针对特定行的评论，每个包含：
       - `path` (字符串): 文件路径
       - `position` (数字): 差异中的行位置
       - `body` (字符串): 评论文本
   - 返回: 创建的审查详细信息

21. `merge_pull_request`
   - 合并一个拉取请求
   - 输入参数：
     - `owner` (字符串): 仓库所有者
     - `repo` (字符串): 仓库名称
     - `pull_number` (数字): 拉取请求编号
     - `commit_title` (可选字符串): 合并提交标题
     - `commit_message` (可选字符串): 合并提交的额外说明
     - `merge_method` (可选字符串): 合并方法 (''merge'', ''squash'', ''rebase'')
   - 返回: 合并结果详细信息

22. `get_pull_request_files`
   - 获取拉取请求中更改的文件列表
   - 输入参数：
     - `owner` (字符串): 仓库所有者
     - `repo` (字符串): 仓库名称
     - `pull_number` (数字): 拉取请求编号
   - 返回: 包含补丁和状态详细信息的更改文件数组

23. `get_pull_request_status`

- 获取拉取请求的所有状态检查的合并状态
  - 输入：
    - `owner` (字符串)：仓库所有者
    - `repo` (字符串)：仓库名称
    - `pull_number` (数字)：拉取请求编号
  - 返回：合并状态检查结果及各检查详情

24. `update_pull_request_branch`
   - 使用基础分支的最新更改更新拉取请求分支（相当于 GitHub 的“更新分支”按钮）
   - 输入：
     - `owner` (字符串)：仓库所有者
     - `repo` (字符串)：仓库名称
     - `pull_number` (数字)：拉取请求编号
     - `expected_head_sha` (可选字符串)：拉取请求 HEAD 引用的预期 SHA
   - 返回：当分支更新成功时返回成功消息

25. `get_pull_request_comments`
   - 获取拉取请求的审查评论
   - 输入：
     - `owner` (字符串)：仓库所有者
     - `repo` (字符串)：仓库名称
     - `pull_number` (数字)：拉取请求编号
   - 返回：包含评论文本、作者以及在差异中的位置等详细信息的拉取请求审查评论数组

26. `get_pull_request_reviews`
   - 获取拉取请求的审查
   - 输入：
     - `owner` (字符串)：仓库所有者
     - `repo` (字符串)：仓库名称
     - `pull_number` (数字)：拉取请求编号
   - 返回：包含审查状态（APPROVED, CHANGES_REQUESTED 等）、审查人和审查正文等详细信息的拉取请求审查数组

## 搜索查询语法

### 代码搜索
- `language:javascript`：按编程语言搜索
- `repo:owner/name`：在特定仓库中搜索
- `path:app/src`：在特定路径中搜索
- `extension:js`：按文件扩展名搜索
- 示例：`q: import express language:typescript path:src/`

### 问题搜索
- `is:issue` 或 `is:pr`：按类型过滤
- `is:open` 或 `is:closed`：按状态过滤
- `label:bug`：按标签搜索
- `author:username`：按作者搜索
- 示例：`q: memory leak is:issue is:open label:bug`

### 用户搜索
- `type:user` 或 `type:org`：按账户类型过滤
- `followers:>1000`：按关注者数量过滤
- `location:London`：按位置搜索
- 示例：`q: fullstack developer location:London followers:>100`

有关详细的搜索语法，请参阅 [GitHub 的搜索文档](https://docs.github.com/en/search-github/searching-on-github)。

## 设置

### 个人访问令牌
[创建具有适当权限的 GitHub 个人访问令牌](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens)：
   - 转到 [个人访问令牌](https://github.com/settings/tokens)（位于 GitHub 设置 > 开发者设置）
   - 选择此令牌应有权访问的仓库（公开、全部或选择）
   - 创建一个具有 `repo` 范围的令牌（“对私有仓库的完全控制”）
     - 或者，如果仅处理公共仓库，请仅选择 `public_repo` 范围
   - 复制生成的令牌

### 与 Claude Desktop 一起使用
要与此 Claude Desktop 一起使用，请将以下内容添加到您的 `claude_desktop_config.json` 文件中：

#### Docker
```json
{
  mcpServers: {
    github: {
      command: docker,
      args: [
        run,
        -i,
        --rm,
        -e,
        GITHUB_PERSONAL_ACCESS_TOKEN,
        mcp/github
      ],
      env: {
        GITHUB_PERSONAL_ACCESS_TOKEN: <YOUR_TOKEN>
      }
    }
  }
}
```


### NPX

```json
{
  mcpServers: {
    github: {
      command: npx,
      args: [
        -y,
        @modelcontextprotocol/server-github
      ],
      env: {
        GITHUB_PERSONAL_ACCESS_TOKEN: <YOUR_TOKEN>
      }
    }
  }
}
```

## 构建

Docker 构建：

```bash
docker build -t mcp/github -f src/github/Dockerfile .
```


## 许可证

此 MCP 服务器根据 MIT 许可证许可。这意味着您可以自由使用、修改和分发该软件，但需遵守 MIT 许可证的条款和条件。更多详情，请参阅项目仓库中的 LICENSE 文件。', '{
  "mcpServers": {
    "github": {
      "command": "npx",
      "args": [
        "-y",
        "@modelcontextprotocol/server-github"
      ],
      "env": {
        "GITHUB_PERSONAL_ACCESS_TOKEN": "<YOUR_TOKEN>"
      }
    }
  }
}', null, 'inner', 'NPX', 0, 'system', 'system', '2025-04-22 11:13:14', 'system', '2025-04-22 11:13:24', 'system', 'https://github.com/modelcontextprotocol/servers/tree/main/src/github', 0, 36);
DELETE
FROM ws_mcp_server_def
WHERE ID = '919';
INSERT INTO ws_mcp_server_def (id, server_code, icon, name, name_en, description, description_en, readme, server_config,
                               tools, type, org_type, deleted, tenant_id, dept_code, created_date, created_by_user_id,
                               last_updated_date, last_updated_by_user_id, url, install_times, view_times)
VALUES ('919', 'GitLab', '', 'GitLab', null, 'GitLab API的MCP服务器，支持项目管理、文件操作等更多功能。', null, '# GitLab MCP 服务器

用于 GitLab API 的 MCP 服务器，支持项目管理、文件操作等功能。

### 功能

- **自动创建分支**：在创建/更新文件或推送更改时，如果分支不存在则会自动创建
- **全面的错误处理**：针对常见问题提供清晰的错误信息
- **保留 Git 历史记录**：操作过程中保持正确的 Git 历史记录，不会强制推送
- **批量操作**：支持单文件和多文件操作


## 工具

1. `create_or_update_file`
   - Create or update a single file in a project
   - Inputs:
     - `project_id` (string): Project ID or URL-encoded path
     - `file_path` (string): Path where to create/update the file
     - `content` (string): Content of the file
     - `commit_message` (string): Commit message
     - `branch` (string): Branch to create/update the file in
     - `previous_path` (optional string): Path of the file to move/rename
   - Returns: File content and commit details

2. `push_files`
   - Push multiple files in a single commit
   - Inputs:
     - `project_id` (string): Project ID or URL-encoded path
     - `branch` (string): Branch to push to
     - `files` (array): Files to push, each with `file_path` and `content`
     - `commit_message` (string): Commit message
   - Returns: Updated branch reference

3. `search_repositories`
   - Search for GitLab projects
   - Inputs:
     - `search` (string): Search query
     - `page` (optional number): Page number for pagination
     - `per_page` (optional number): Results per page (default 20)
   - Returns: Project search results

4. `create_repository`
   - Create a new GitLab project
   - Inputs:
     - `name` (string): Project name
     - `description` (optional string): Project description
     - `visibility` (optional string): ''private'', ''internal'', or ''public''
     - `initialize_with_readme` (optional boolean): Initialize with README
   - Returns: Created project details

5. `get_file_contents`
   - Get contents of a file or directory
   - Inputs:
     - `project_id` (string): Project ID or URL-encoded path
     - `file_path` (string): Path to file/directory
     - `ref` (optional string): Branch/tag/commit to get contents from
   - Returns: File/directory contents

6. `create_issue`
   - Create a new issue
   - Inputs:
     - `project_id` (string): Project ID or URL-encoded path
     - `title` (string): Issue title
     - `description` (optional string): Issue description
     - `assignee_ids` (optional number[]): User IDs to assign
     - `labels` (optional string[]): Labels to add
     - `milestone_id` (optional number): Milestone ID
   - Returns: Created issue details

7. `create_merge_request`
   - Create a new merge request
   - Inputs:
     - `project_id` (string): Project ID or URL-encoded path
     - `title` (string): MR title
     - `description` (optional string): MR description
     - `source_branch` (string): Branch containing changes
     - `target_branch` (string): Branch to merge into
     - `draft` (optional boolean): Create as draft MR
     - `allow_collaboration` (optional boolean): Allow commits from upstream members
   - Returns: Created merge request details

8. `fork_repository`
   - Fork a project
   - Inputs:
     - `project_id` (string): Project ID or URL-encoded path
     - `namespace` (optional string): Namespace to fork to
   - Returns: Forked project details

9. `create_branch`
   - Create a new branch
   - Inputs:
     - `project_id` (string): Project ID or URL-encoded path
     - `branch` (string): Name for new branch
     - `ref` (optional string): Source branch/commit for new branch
   - Returns: Created branch reference

## 设置

### 个人访问令牌
[创建一个具有适当权限的 GitLab 个人访问令牌](https://docs.gitlab.com/ee/user/profile/personal_access_tokens.html)：
   - 进入 GitLab 的用户设置 > 访问令牌
   - 选择所需的作用域：
     - `api` 用于完全 API 访问
     - `read_api` 用于只读访问
     - `read_repository` 和 `write_repository` 用于仓库操作
   - 创建令牌并安全保存

### 与 Claude Desktop 一起使用
将以下内容添加到您的 `claude_desktop_config.json` 中：

#### Docker
```json
{
  mcpServers: {
    gitlab: {
      command: docker,
      args: [
        run,
        --rm,
        -i,
        -e,
        GITLAB_PERSONAL_ACCESS_TOKEN,
        -e,
        GITLAB_API_URL,
        mcp/gitlab
      ],
      env: {
        GITLAB_PERSONAL_ACCESS_TOKEN: <YOUR_TOKEN>,
        GITLAB_API_URL: https://gitlab.com/api/v4 // Optional, for self-hosted instances
      }
    }
  }
}
```


### NPX

```json
{
  mcpServers: {
    gitlab: {
      command: npx,
      args: [
        -y,
        @modelcontextprotocol/server-gitlab
      ],
      env: {
        GITLAB_PERSONAL_ACCESS_TOKEN: <YOUR_TOKEN>,
        GITLAB_API_URL: https://gitlab.com/api/v4 // Optional, for self-hosted instances
      }
    }
  }
}
```


## 构建

Docker 构建：

```bash
docker build -t vonwig/gitlab:mcp -f src/gitlab/Dockerfile .
```


## 环境变量

- `GITLAB_PERSONAL_ACCESS_TOKEN`: 您的 GitLab 个人访问令牌（必需）
- `GITLAB_API_URL`: GitLab API 的基础 URL（可选，默认为 `https://gitlab.com/api/v4`）

## 许可证

此 MCP 服务器依据 MIT 许可证发布。这意味着您可以自由地使用、修改和分发该软件，但需遵守 MIT 许可证的条款和条件。更多详情，请参阅项目仓库中的 LICENSE 文件。', '{
  "mcpServers": {
    "gitlab": {
      "command": "npx",
      "args": ["-y", "@yoda.digital/gitlab-mcp-server"],
      "env": {
        "GITLAB_PERSONAL_ACCESS_TOKEN": "your_token_here",
        "GITLAB_API_URL": "https://gitlab.com/api/v4"
      },
      "alwaysAllow": [],
      "disabled": false
    }
  }
}', null, 'inner', 'NPX', 0, 'system', 'system', '2025-04-22 11:13:13', 'system', '2025-04-22 11:13:24', 'system', 'https://github.com/modelcontextprotocol/servers/tree/main/src/gitlab', 0, 20);
DELETE
FROM ws_mcp_server_def
WHERE ID = '926';
INSERT INTO ws_mcp_server_def (id, server_code, icon, name, name_en, description, description_en, readme, server_config,
                               tools, type, org_type, deleted, tenant_id, dept_code, created_date, created_by_user_id,
                               last_updated_date, last_updated_by_user_id, url, install_times, view_times)
VALUES ('926', 'Filesystem', '', '文件系统', null, '实现用于文件系统操作的模型上下文协议（MCP）的 Node.js 服务器。', null, '# 文件系统 MCP 服务器

Node.js 服务器实现了用于文件系统操作的模型上下文协议 (MCP)。

## 特性

- 读写文件
- 创建/列出/删除目录
- 移动文件/目录
- 搜索文件
- 获取文件元数据

**注意**：服务器仅允许在通过 `args` 指定的目录内进行操作。

## API

### 资源

- `file://system`: 文件系统操作接口

### 工具

- **read_file**
  - 读取文件的全部内容
  - 输入: `path` (字符串)
  - 使用 UTF-8 编码读取整个文件的内容

- **read_multiple_files**
  - 同时读取多个文件
  - 输入: `paths` (字符串数组)
  - 失败的读取不会停止整个操作

- **write_file**
  - 创建新文件或覆盖现有文件（使用时需谨慎）
  - 输入:
    - `path` (字符串): 文件位置
    - `content` (字符串): 文件内容

- **edit_file**
  - 使用高级模式匹配和格式化进行选择性编辑
  - 功能:
    - 基于行和多行内容匹配
    - 保留缩进的空白字符规范化
    - 带置信度评分的模糊匹配
    - 正确定位的多个同时编辑
    - 缩进样式检测和保留
    - 带上下文的 Git 风格差异输出
    - 干运行模式预览更改
    - 带置信度评分的失败匹配调试
  - 输入:
    - `path` (字符串): 要编辑的文件
    - `edits` (数组): 编辑操作列表
      - `oldText` (字符串): 要搜索的文本（可以是子字符串）
      - `newText` (字符串): 要替换为的文本
    - `dryRun` (布尔值): 不应用更改的情况下预览更改 (默认: false)
    - `options` (对象): 可选的格式设置
      - `preserveIndentation` (布尔值): 保持现有的缩进 (默认: true)
      - `normalizeWhitespace` (布尔值): 在保留结构的同时规范化空格 (默认: true)
      - `partialMatch` (布尔值): 启用模糊匹配 (默认: true)
  - 返回详细的差异和匹配信息（干运行模式），否则应用更改
  - 最佳实践：在应用更改之前，始终先使用干运行模式预览更改

- **create_directory**
  - 创建新目录或确保其存在
  - 输入: `path` (字符串)
  - 如果需要，创建父目录
  - 如果目录已存在，则静默成功

- **list_directory**
  - 列出带有 [FILE] 或 [DIR] 前缀的目录内容
  - 输入: `path` (字符串)

- **move_file**
  - 移动或重命名文件和目录
  - 输入:
    - `source` (字符串)
    - `destination` (字符串)
  - 如果目标已存在则失败

- **search_files**
  - 递归搜索文件/目录
  - 输入:
    - `path` (字符串): 起始目录
    - `pattern` (字符串): 搜索模式
    - `excludePatterns` (字符串数组): 排除任何模式。支持 Glob 格式。
  - 匹配不区分大小写
  - 返回匹配项的完整路径

- **get_file_info**
  - 获取详细的文件/目录元数据
  - 输入: `path` (字符串)
  - 返回:
    - 大小
    - 创建时间
    - 修改时间
    - 访问时间
    - 类型 (文件/目录)
    - 权限

- **list_allowed_directories**
  - 列出服务器允许访问的所有目录
  - 无需输入
  - 返回:
    - 该服务器可以从中读取/写入的目录

## 与 Claude Desktop 一起使用
将以下内容添加到您的 `claude_desktop_config.json` 中：

```json
{
  // 配置内容
}
```

注意：你可以通过将目录挂载到 `/projects` 来为服务器提供沙箱目录。添加 `ro` 标志会使该目录对服务器只读。

### Docker
注意：默认情况下，所有目录都必须挂载到 `/projects`。

```json
{
  mcpServers: {
    filesystem: {
      command: docker,
      args: [
        run,
        -i,
        --rm,
        --mount, type=bind,src=/Users/username/Desktop,dst=/projects/Desktop,
        --mount, type=bind,src=/path/to/other/allowed/dir,dst=/projects/other/allowed/dir,ro,
        --mount, type=bind,src=/path/to/file.txt,dst=/projects/path/to/file.txt,
        mcp/filesystem,
        /projects
      ]
    }
  }
}
```


### NPX

```json
{
  mcpServers: {
    filesystem: {
      command: npx,
      args: [
        -y,
        @modelcontextprotocol/server-filesystem,
        /Users/username/Desktop,
        /path/to/other/allowed/dir
      ]
    }
  }
}
```


## 构建

Docker 构建：

```bash
docker build -t mcp/filesystem -f src/filesystem/Dockerfile .
```


## 许可证

此 MCP 服务器根据 MIT 许可证授权。这意味着你可以在遵守 MIT 许可证条款和条件的前提下自由使用、修改和分发该软件。更多详情，请参见项目仓库中的 LICENSE 文件。', '{
  "mcpServers": {
    "filesystem": {
      "command": "npx",
      "args": [
        "-y",
        "@modelcontextprotocol/server-filesystem",
        "/app",
        "/app"
      ]
    }
  }
}', null, 'inner', 'NPX', 0, 'system', 'system', '2025-04-22 11:09:20', 'system', '2025-04-22 11:13:24', 'system', '', 55, 95);
DELETE
FROM ws_mcp_server_def
WHERE ID = '929';
INSERT INTO ws_mcp_server_def (id, server_code, icon, name, name_en, description, description_en, readme, server_config,
                               tools, type, org_type, deleted, tenant_id, dept_code, created_date, created_by_user_id,
                               last_updated_date, last_updated_by_user_id, url, install_times, view_times)
VALUES ('929', 'alipay', '', '支付宝MCP', null,
        '是支付宝开放平台提供的 MCP Server，让你可以轻松将支付宝开放平台提供的交易创建、查询、退款等能力集成到你的 LLM 应用中，并进一步创建具备支付能力的智能工具。',
        null, '## 1. 简介

`@alipay/mcp-server-alipay` 是支付宝开放平台提供的 MCP Server，让你可以轻松将支付宝开放平台提供的交易创建、查询、退款等能力集成到你的 LLM 应用中，并进一步创建具备支付能力的智能工具。

以下是一个虚构的简化使用场景，用于方便理解工具能力：

> 一位插画师希望通过提供定制的原创插画服务谋取收入。传统方式下，他/她需要和每位客户反复沟通需求、确定价格，并发送支付链接，然后再人工确认支付情况，这个过程繁琐且费时。
>
> 现在，插画师利用支付宝 MCP Server 与智能 Agent 工具，通过 Agent 搭建平台，开发了一个智能聊天应用（网页或小程序）。客户只需在应用中描述自己的绘画需求（如风格偏好、插画用途、交付时间等），AI 就会自动分析需求，快速生成准确且合理的定制报价，并通过工具即时创建出专用的支付宝支付链接。
>
> 客户点击并支付后，创作者立即收到通知，进入创作环节。无需人工往返对话确认交易状态或支付情况，整个流程不仅便捷顺畅，还能显著提高交易效率和客户满意度，让插画师更专注于自己的创作本身，实现更轻松的个性化服务商业模式。

```
     最终用户设备                     Agent 运行环境
+---------------------+        +--------------------------+      +-------------------+
|                     |  交流  |   支付宝 MCP Server +    |      |                   |
|    小程序/WebApp    |<------>|   其他 MCP Server +      |<---->|     支付服务      |
|                     |  支付  |   Agent 开发工具         |      |   交易/退款/查询  |
+---------------------+        +--------------------------+      +-------------------+
     创作服务买家                     智能工具开发者                   支付宝开放平台
      (最终用户)                         (创作者)
```

关于本工具的更多介绍和使用指南，包括准备收款商户身份等前置流程，请参考支付宝开放平台上的 [支付 MCP 服务文档](https://opendocs.alipay.com/open/0go80l) 。

## 2. 使用和配置

要使用工具的大部分支付能力，你需要先成为支付宝开放平台的收款商户，获取商户私钥。
之后，你可以直接在主流的 MCP Client 上使用支付宝 MCP Server：

### 在 Cursor 中使用

在 Cursor 项目中的 `.cursor/mcp.json` 加入如下配置：

```json
{
  mcpServers: {
    mcp-server-alipay: {
      command: npx,
      args: [-y, @alipay/mcp-server-alipay],
      env: {
        AP_APP_ID: 2014...222,
        AP_APP_KEY: MIIE...DZdM=,
        AP_PUB_KEY: MIIB...DAQAB,
        AP_RETURN_URL: https://success-page,
        AP_NOTIFY_URL: https://your-own-server,
        ...其他参数: ...其他值
      }
    },
    其他工具: {
      ...: ...
    }
  }
}
```

### 在 Cline 中使用

在你的 Cline 设置中找到 `cline_mcp_settings.json` 配置文件，并加入如下配置：

```json
{
  mcpServers: {
    mcp-server-alipay: {
      command: npx,
      args: [-y, @alipay/mcp-server-alipay],
      env: {
        AP_APP_ID: 2014...222,
        AP_APP_KEY: MIIE...DZdM=,
        AP_PUB_KEY: MIIB...DAQAB,
        AP_RETURN_URL: https://success-page,
        AP_NOTIFY_URL: https://your-own-server,
        ...其他参数: ...其他值
      },
      disable: false,
      autoApprove: []
    },
    其他工具: {
      ...: ...
    }
  }
}
```

### 在其他 MCP Client 中使用

你也可以在任何其它 MCP Client 中使用，合理配置 Server 进程启动方式 `npx -y @alipay/mcp-server-alipay`，并按下文介绍设置环境参数即可。

### 所有参数

支付宝 MCP Server 通过环境变量接收参数。参数和默认值包括:

```shell
# 支付宝开放平台配置

AP_APP_ID=2014...222                    # 商户在开放平台申请的应用 ID（APPID）。必需。
AP_APP_KEY=MIIE...DZdM=                 # 商户在开放平台申请的应用私钥。必需。
AP_PUB_KEY=MIIB...DAQAB                 # 用于验证支付宝服务端数据签名的支付宝公钥，在开放平台获取。必需。
AP_RETURN_URL=https://success-page      # 网页支付完成后对付款用户展示的「同步结果返回地址」。
AP_NOTIFY_URL=https://your-own-server   # 支付完成后，用于告知开发者支付结果的「异步结果通知地址」。
AP_ENCRYPTION_ALGO=RSA2                 # 商户在开放平台配置的参数签名方式。可选值为 RSA2 或 RSA。缺省值为 RSA2。
AP_CURRENT_ENV=prod                     # 连接的支付宝开放平台环境。可选值为 prod（线上环境）或 sandbox（沙箱环境）。缺省值为 prod。

# MCP Server 配置

AP_SELECT_TOOLS=all                      # 允许使用的工具。可选值为 all 或逗号分隔的工具名称列表。工具名称包括 `mobilePay`, `webPagePay`, `queryPay`, `refundPay`, `refundQuery`。缺省值为 all。
AP_LOG_ENABLED=true                      # 是否在 $HOME/mcp-server-alipay.log 中记录日志。默认值为 true。
```

## 3. 使用 MCP Inspector 调试

你可以使用 MCP Inspector 来调试和了解支付宝 MCP Server 的功能：

1. 通过 `export` 设置各环境变量；
2. 执行 `npx -y @modelcontextprotocol/inspector npx -y @alipay/mcp-server-alipay` 启动 MCP Inspector；
3. 在 MCP Inspector WebUI 中调试即可。

## 4. 支持的能力

以下表格列出了所有可用的支付工具能力：

| 名称 | `AP_SELECT_TOOLS` 中的工具名称 | 描述 | 参数 | 输出 |
|-------|--------------------------|------|------|------|
| `create-mobile-alipay-payment` | `mobilePay` | 创建一笔支付宝订单，返回带有支付链接的 Markdown 文本，该链接在手机浏览器中打开后可跳转到支付宝或直接在浏览器中支付。本工具适用于移动网站或移动 App。 | - outTradeNo: 商户订单号，最长 64 个字符<br>- totalAmount: 支付金额，单位：元，最小 0.01<br>- orderTitle: 订单标题，最长 256 个字符 | - url: 支付链接的 markdown 文本 |
| `create-web-page-alipay-payment` | `webPagePay` | 创建一笔支付宝订单，返回带有支付链接的 Markdown 文本，该链接在电脑浏览器中打开后会展示支付二维码，用户可扫码支付。本工具适用于桌面网站或电脑客户端。 | - outTradeNo: 商户订单号，最长 64 个字符<br>- totalAmount: 支付金额，单位：元，最小 0.01<br>- orderTitle: 订单标题，最长 256 个字符 | - url: 支付链接的 markdown 文本 |
| `query-alipay-payment` | `queryPay` | 查询一笔支付宝订单，并返回带有订单信息的文本 | - outTradeNo: 商户订单号，最长 64 个字符 | - tradeStatus: 订单的交易状态<br>- totalAmount: 订单的交易金额<br>- tradeNo: 支付宝交易号 |
| `refund-alipay-payment` | `refundPay` | 对交易发起退款，并返回退款状态和退款金额 | - outTradeNo: 商户订单号，最长 64 个字符<br>- refundAmount: 退款金额，单位：元，最小 0.01<br>- outRequestNo: 退款请求号，最长 64 个字符<br>- refundReason: 退款原因，最长 256 个字符（可选） | - tradeNo: 支付宝交易号<br>- refundResult: 退款结果 |
| `query-alipay-refund` | `refundQuery` | 查询一笔支付宝退款，并返回退款状态和退款金额 | - outRequestNo: 退款请求号，最长 64 个字符<br>- outTradeNo: 商户订单号，最长 64 个字符 | - tradeNo: 支付宝交易号<br>- refundAmount: 退款金额<br>- refundStatus: 退款状态 |

## 5. 如何选择合适的支付方式

在开发过程中，为了让 LLM 能更准确地选择合适的支付方式，建议在 Prompt 中清晰说明你的产品使用场景：

- **扫码支付（`webPagePay`）**：适用于用户在电脑屏幕上看到支付界面的场景。如果您的应用或网站主要运行在桌面端（PC），你可以在 Prompt 中说明：我的应用是桌面软件/PC网站，需要在电脑上展示支付二维码。

- **手机支付（`mobilePay`）**：适用于用户在手机浏览器内发起支付的场景。如果您的应用是手机H5页面或移动端网站，你可以在 Prompt 中说明：我的页面是手机网页，需要直接在手机上唤起支付宝支付。

我们会在未来提供更多适合 AI 应用的支付方式，敬请期待。

## 6. 注意事项

* 支付宝 MCP 服务目前处于发布早期阶段，相关能力和配套设施正在持续完善中。如有问题反馈、使用体验或建议，欢迎在 [支付宝开发者社区](https://open.alipay.com/portal/forum) 参与讨论。
* 部署和使用智能体服务时，请务必妥善保管自己的商户私钥，防止泄露。如需要，可参考 [支付宝开放平台-如何修改密钥](https://opendocs.alipay.com/support/01rav9) 的说明让已有密钥失效。
* 在开发任何使用 MCP Server 的智能体服务，并提供给用户使用时，请了解必要的安全知识，防范 AI 应用特有的 Prompt 攻击、MCP Server 任意命令执行等安全风险。
* 更多注意事项和最佳实践，请参考支付宝开放平台上 [关于支付 MCP 服务](https://opendocs.alipay.com/open/0go80l) 的说明。

## 7. 使用协议

本工具是支付宝开放平台能力的组成部分。使用期间，请遵守 [支付宝开放平台开发者服务协议](https://ds.alipay.com/fd-ifz2dlhv/index.html) 及相关商业行为法规。', '{
  "mcpServers": {
    "mcp-server-alipay": {
      "command": "npx",
      "args": ["-y", "@alipay/mcp-server-alipay"],
      "env": {
        "AP_APP_ID": "2014...222",
        "AP_APP_KEY": "MIIE...DZdM=",
        "AP_PUB_KEY": "MIIB...DAQAB",
        "AP_RETURN_URL": "https://success-page",
        "AP_NOTIFY_URL": "https://your-own-server"      }
    }  }
}', null, 'inner', 'NPX', 0, 'system', 'system', '2025-04-22 11:06:20', 'system', '2025-04-22 11:13:24', 'system', 'https://www.npmjs.com/package/@alipay/mcp-server-alipay?activeTab=readme', 1, 12);
DELETE
FROM ws_mcp_server_def
WHERE ID = '993';
INSERT INTO ws_mcp_server_def (id, server_code, icon, name, name_en, description, description_en, readme, server_config,
                               tools, type, org_type, deleted, tenant_id, dept_code, created_date, created_by_user_id,
                               last_updated_date, last_updated_by_user_id, url, install_times, view_times)
VALUES ('993', 'AmapMaps', '', '高德地图', null,
        '高德地图MCP Server现已覆盖12大核心接口，提供全场景覆盖的地理信息服务，包括地理编码、逆地理编码、IP定位、天气查询、骑行路径规划、步行路径规划、驾车路径规划、公交路径规划、距离测量、关键词搜索、周边搜索、详情搜索等。',
        null, '## 产品介绍

为实现 LBS 服务与 LLM 更好的交互，高德地图 MCP Server 现已覆盖12大核心服务接口，提供全场景覆盖的地图服务，包括地理编码、逆地理编码、IP 定位、天气查询、骑行路径规划、步行路径规划、驾车路径规划、公交路径规划、距离测量、关键词搜索、周边搜索、详情搜索等。

为进一步提高开发者接入效率与体验，高德地图开放平台为开发者提供了[通用级 SSE 协议](https://lbs.amap.com/api/mcp-server/gettingstarted) MCP 服务解决方案。

## 高德地图开放平台通用级 SSE 协议 MCP 服务解决方案

#### 产品架构图

![](https://a.amap.com/lbs/static/img/doc/doc_1743685971070_d2b5c.png)

#### 什么是 SSE？

Server-Sent Events（SSE，服务器发送事件）是一种基于 HTTP 协议的技术，允许服务器向客户端单向、实时地推送数据。在 SSE 模式下，开发者可以在客户端通过创建一个 EventSource 对象与服务器建立持久连接，服务器则通过该连接持续发送数据流，而无需客户端反复发送请求。

#### 产品特点

+   使用简单：适用普通用户基于MCP（SSE）方式，不必部署本地服务，简单通过 URL 地址配置即可使用。

+   自动升级：我们会持续进行迭代更新，无须用户自己任何额外操作使用。

+   更易于大模型理解：我们对原始的JSON结果进行了语义化的转换，更易于大模型理解内容。

+   零运维成本：采用全托管云服务架构，用户无需关心服务器维护、资源扩容等底层运维问题。

+   协议兼容：支持SSE长连接，适配不同业务场景的技术需求。


前往 [快速接入](https://lbs.amap.com/api/mcp-server/gettingstarted) 文档，了解如何接入 MCP Server SSE 服务

## 能力介绍

#### 地理编码

将详细的结构化地址转换为经纬度坐标。

<table><colgroup><col width=112><col width=802></colgroup><tbody><tr><td colspan=1 rowspan=1><p><span data-type=text>输入</span></p></td><td colspan=1 rowspan=1><p><span data-type=text>address&nbsp;(位置信息)，</span><span data-type=text>city&nbsp;(城市信息，非必须)</span></p></td></tr><tr><td colspan=1 rowspan=1><p><span data-type=text>输出</span></p></td><td colspan=1 rowspan=1><p><span data-type=text>location&nbsp;(位置经纬度)</span></p></td></tr></tbody></table>

#### 逆地理编码

将一个高德经纬度坐标转换为行政区划地址信息。

<table><colgroup><col width=114><col width=765></colgroup><tbody><tr><td colspan=1 rowspan=1><p><span data-type=text>输入</span></p></td><td colspan=1 rowspan=1><p><span data-type=text>location&nbsp;(位置经纬度)</span></p></td></tr><tr><td colspan=1 rowspan=1><p><span data-type=text>输出</span></p></td><td colspan=1 rowspan=1><p><span data-type=text>addressComponent</span><span data-type=text>&nbsp;(位置信息，包括省市区等信息)</span></p></td></tr></tbody></table>

#### IP 定位

IP 定位根据用户输入的 IP 地址，定位 IP 的所在位置。

<table><colgroup><col width=113><col width=799></colgroup><tbody><tr><td colspan=1 rowspan=1><p><span data-type=text>输入</span></p></td><td colspan=1 rowspan=1><p><span data-type=text>IP</span></p></td></tr><tr><td colspan=1 rowspan=1><p><span data-type=text>输出</span></p></td><td colspan=1 rowspan=1><p><span data-type=text>province&nbsp;(省)，city&nbsp;(城市)，adcode&nbsp;(城市编码)</span></p></td></tr></tbody></table>

#### 天气查询

根据城市名称或者标准adcode查询指定城市的天气。

<table><colgroup><col width=117><col width=802></colgroup><tbody><tr><td colspan=1 rowspan=1><p><span data-type=text>输入</span></p></td><td colspan=1 rowspan=1><p><span data-type=text>city</span><span data-type=text>&nbsp;(城市名称或城市adcode)</span></p></td></tr><tr><td colspan=1 rowspan=1><p><span data-type=text>输出</span></p></td><td colspan=1 rowspan=1><p><span data-type=text>forecasts&nbsp;(预报天气)</span></p></td></tr></tbody></table>

#### 骑行路径规划

用于规划骑行通勤方案，规划时会考虑天桥、单行线、封路等情况。最大支持 500km 的骑行路线规划。

<table><colgroup><col width=116><col width=801></colgroup><tbody><tr><td colspan=1 rowspan=1><p><span data-type=text>输入</span></p></td><td colspan=1 rowspan=1><p><span data-type=text>origin</span><span data-type=text>&nbsp;(起点经纬度)，</span><span data-type=text>destination&nbsp;(终点经纬度)</span></p></td></tr><tr><td colspan=1 rowspan=1><p><span data-type=text>输出</span></p></td><td colspan=1 rowspan=1><p><span data-type=text>distance&nbsp;(规划距离)，duration&nbsp;(规划时间)，steps&nbsp;(规划步骤信息)</span></p></td></tr></tbody></table>

#### 步行路径规划

可以根据输入起点终点经纬度坐标，规划100km 以内的步行通勤方案，并且返回通勤方案的数据。

<table><colgroup><col width=118><col width=804></colgroup><tbody><tr><td colspan=1 rowspan=1><p><span data-type=text>输入</span></p></td><td colspan=1 rowspan=1><p><span data-type=text>origin</span><span data-type=text>&nbsp;(起点经纬度)，</span><span data-type=text>destination&nbsp;(终点经纬度)</span></p></td></tr><tr><td colspan=1 rowspan=1><p><span data-type=text>输出</span></p></td><td colspan=1 rowspan=1><p><span data-type=text>origin&nbsp;(</span><span data-type=text>起点</span><span data-type=text>信息)，destination&nbsp;(终点信息)，paths&nbsp;(规划具体信息)</span></p></td></tr></tbody></table>

#### 驾车路径规划

根据用户起终点经纬度坐标规划以小客车、轿车通勤出行的方案，并且返回通勤方案的数据。

<table><colgroup><col width=118><col width=803></colgroup><tbody><tr><td colspan=1 rowspan=1><p><span data-type=text>输入</span></p></td><td colspan=1 rowspan=1><p><span data-type=text>origin</span><span data-type=text>&nbsp;(起点经纬度)，</span><span data-type=text>destination&nbsp;(终点经纬度)</span></p></td></tr><tr><td colspan=1 rowspan=1><p><span data-type=text>输出</span></p></td><td colspan=1 rowspan=1><p><span data-type=text>origin&nbsp;(</span><span data-type=text>起点</span><span data-type=text>信息)，destination&nbsp;(终点信息)，paths&nbsp;(规划具体信息)</span></p></td></tr></tbody></table>

#### 公交路径规划

根据用户起终点经纬度坐标规划综合各类公共（火车、公交、地铁）交通方式的通勤方案，并且返回通勤方案的数据，跨城场景下必须传起点城市与终点城市。

<table><colgroup><col width=121><col width=675></colgroup><tbody><tr><td colspan=1 rowspan=1><p><span data-type=text>输入</span></p></td><td colspan=1 rowspan=1><p><span data-type=text>origin&nbsp;(起点经纬度)，destination&nbsp;(终点经纬度)，city&nbsp;(起点城市)，cityd&nbsp;(终点城市)</span></p></td></tr><tr><td colspan=1 rowspan=1><p><span data-type=text>输出</span></p></td><td colspan=1 rowspan=1><p><span data-type=text>origin&nbsp;(</span><span data-type=text>起点</span><span data-type=text>信息)，destination&nbsp;(终点信息)，distance&nbsp;(规划距离)，transits&nbsp;(规划具体信息)</span></p></td></tr></tbody></table>

#### 距离测量

测量两个经纬度坐标之间的距离。

<table><colgroup><col width=120><col width=752></colgroup><tbody><tr><td colspan=1 rowspan=1><p><span data-type=text>输入</span></p></td><td colspan=1 rowspan=1><p><span data-type=text>origin&nbsp;(起点经纬度)，destination&nbsp;(终点经纬度)</span></p></td></tr><tr><td colspan=1 rowspan=1><p><span data-type=text>输出</span></p></td><td colspan=1 rowspan=1><p><span data-type=text>origin_id&nbsp;(</span><span data-type=text>起点</span><span data-type=text>信息)，dest_id&nbsp;(终点信息)，distance&nbsp;(规划距离)，duration&nbsp;(时间)</span></p></td></tr></tbody></table>

#### 关键词搜索

根据用户传入关键词，搜索出相关的POI地点信息。

<table><colgroup><col width=122><col width=822></colgroup><tbody><tr><td colspan=1 rowspan=1><p><span data-type=text>输入</span></p></td><td colspan=1 rowspan=1><p><span data-type=text>keywords</span><span data-type=text>&nbsp;(</span><span data-type=text>搜索关键词</span><span data-type=text>)，</span><span data-type=text>city&nbsp;(查询城市，非必须)</span></p></td></tr><tr><td colspan=1 rowspan=1><p><span data-type=text>输出</span></p></td><td colspan=1 rowspan=1><p><span data-type=text>suggestion&nbsp;(搜索建议)，pois&nbsp;(地点信息列表)</span></p></td></tr></tbody></table>

#### 周边搜索

根据用户传入关键词以及坐标location，搜索出radius半径范围的POI地点信息。

<table><colgroup><col width=118><col width=813></colgroup><tbody><tr><td colspan=1 rowspan=1><p><span data-type=text>输入</span></p></td><td colspan=1 rowspan=1><p><span data-type=text>keywords</span><span data-type=text>&nbsp;(</span><span data-type=text>搜索关键词</span><span data-type=text>)，</span><span data-type=text>location&nbsp;(中心点经度纬度)，radius&nbsp;(搜索半径，非必须)</span></p></td></tr><tr><td colspan=1 rowspan=1><p><span data-type=text>输出</span></p></td><td colspan=1 rowspan=1><p><span data-type=text>pois&nbsp;(地点信息列表)</span></p></td></tr></tbody></table>

#### 详情搜索

查询关键词搜或者周边搜获取到的POI ID的详细信息。

<table><colgroup><col width=113><col width=826></colgroup><tbody><tr><td colspan=1 rowspan=1><p><span data-type=text>输入</span></p></td><td colspan=1 rowspan=1><p><span data-type=text>id</span><span data-type=text>&nbsp;(</span><span data-type=text>关键词搜或周边搜获取的poiid</span><span data-type=text>)</span></p></td></tr><tr><td colspan=1 rowspan=1><p><span data-type=text>输出</span></p></td><td colspan=1 rowspan=1><p><span data-type=text>地点详情信息</span></p><p><span data-type=text>location&nbsp;(地点经纬度)，address&nbsp;(地址)，business_area&nbsp;(商圈)，city(城市)，type&nbsp;(地点类型)&nbsp;等</span></p></td></tr></tbody></table>', '{
  "mcpServers": {
    "amap-maps": {
      "command": "npx",
      "args": [
        "-y",
        "@amap/amap-maps-mcp-server"
      ],
      "env": {
        "AMAP_MAPS_API_KEY": "api_key"
      }
    }
  }
}', null, 'inner', 'NPX', 0, 'system', 'system', '2025-04-22 11:05:20', 'system', '2025-04-22 11:13:24', 'system', 'https://www.npmjs.com/package/@amap/amap-maps-mcp-server', 5, 26);
DELETE
FROM ws_mcp_server_def
WHERE ID = '937';
INSERT INTO ws_mcp_server_def (id, server_code, icon, name, name_en, description, description_en, readme, server_config,
                               tools, type, org_type, deleted, tenant_id, dept_code, created_date, created_by_user_id,
                               last_updated_date, last_updated_by_user_id, url, install_times, view_times)
VALUES ('937', 'EdgeOnePages', '', 'EdgeOne-Pages', null, '基于 EdgeOne Pages 的 MCP 服务器，支持代码部署为在线页面。',
        null, '# EdgeOne Pages MCP

一个用于将 HTML 内容部署到 EdgeOne Pages 并获取公开可访问 URL 的 MCP 服务。

<a href=https://glama.ai/mcp/servers/@TencentEdgeOne/edgeone-pages-mcp>
  <img width=380 height=200 src=https://glama.ai/mcp/servers/@TencentEdgeOne/edgeone-pages-mcp/badge alt=EdgeOne Pages MCP 服务器 />
</a>

## 演示

![](https://cloudcache.tencent-cloud.com/qcloud/ui/static/static_source_business/04ff9814-bcd3-442c-a2d0-eefd4ee1b13c.gif)

## 要求

* Node.js 18 或更高版本

## 配置 MCP

```json
{
  mcpServers: {
    edgeone-pages-mcp-server: {
      command: npx,
      args: [edgeone-pages-mcp]
    }
  }
}
```

## 架构

![EdgeOne Pages MCP 架构](./assets/architecture.svg)

架构图展示了工作流程：
1. 大型语言模型生成 HTML 内容
2. 内容发送到 EdgeOne Pages MCP 服务器
3. MCP 服务器将内容部署到 EdgeOne Pages 边缘函数
4. 内容存储在 EdgeOne KV Store 以便快速边缘访问
5. MCP 服务器返回一个公共 URL
6. 用户可以通过浏览器访问已部署的内容并享受快速边缘交付

## 功能

* 通过 MCP 协议快速将 HTML 内容部署到 EdgeOne Pages
* 自动生成公开可访问的 URL

## 实现

此 MCP 服务集成了 EdgeOne Pages Functions 以部署静态 HTML 内容。实现方式使用：

1. **EdgeOne Pages Functions** - 一种无服务器计算平台，允许在边缘执行 JavaScript/TypeScript 代码。

2. **关键实现细节**：
   - 使用 EdgeOne Pages KV 存储并提供 HTML 内容
   - 为每次部署自动生成一个公共 URL
   - 使用适当的错误消息处理 API 错误

3. **工作原理**：
   - MCP 服务器通过 `deploy-html` 工具接受 HTML 内容
   - 它连接到 EdgeOne Pages API 以获取基础 URL
   - 使用 EdgeOne Pages KV API 部署 HTML 内容
   - 返回一个可公开访问的已部署内容的 URL

4. **使用示例**：
   - 向 MCP 服务提供 HTML 内容
   - 接收一个可立即访问的公共 URL

有关更多信息，请参阅 [EdgeOne Pages Functions 文档](https://edgeone.ai/document/162227908259442688) 和 [EdgeOne Pages KV 存储指南](https://edgeone.ai/document/162227803822321664)。

## 许可证

MIT', '{
  "mcpServers": {
    "edgeone-pages-mcp-server": {
      "command": "npx",
      "args": ["edgeone-pages-mcp"]
    }
  }
}', null, 'inner', 'NPX', 0, 'system', 'system', '2025-04-27 21:52:59', 'system', '2025-04-27 21:53:59', 'system', 'https://github.com/TencentEdgeOne/edgeone-pages-mcp/tree/main', 1, 20);
DELETE
FROM ws_mcp_server_def
WHERE ID = '939';
INSERT INTO ws_mcp_server_def (id, server_code, icon, name, name_en, description, description_en, readme, server_config,
                               tools, type, org_type, deleted, tenant_id, dept_code, created_date, created_by_user_id,
                               last_updated_date, last_updated_by_user_id, url, install_times, view_times)
VALUES ('939', 'Tavily', '', 'Tavily智搜', null,
        '该服务器使AI系统能够与Tavily的搜索和数据提取工具集成，提供实时的网络信息访问和领域特定的搜索。', null, '# Tavily MCP 服务器 ð

![GitHub Repo stars](https://img.shields.io/github/stars/tavily-ai/tavily-mcp?style=social)
![npm](https://img.shields.io/npm/dt/tavily-mcp)
![smithery badge](https://smithery.ai/badge/@tavily-ai/tavily-mcp)

> ð **兼容 [Cline](https://github.com/cline/cline), [Cursor](https://cursor.sh), [Claude Desktop](https://claude.ai/desktop) 以及其他任何 MCP 客户端！**
>
> Tavily MCP 也与任何 MCP 客户端兼容
>
> ð  结合 Tavily MCP 与 Neo4j MCP 服务器的 [教程](https://medium.com/@dustin_36183/building-a-knowledge-graph-assistant-combining-tavily-and-neo4j-mcp-servers-with-claude-db92de075df9)!
>
> ð  在 VS Code 中将 Tavily MCP 与 Cline 集成的 [教程](https://medium.com/@dustin_36183/connect-your-coding-assistant-to-the-web-integrating-tavily-mcp-with-cline-in-vs-code-5f923a4983d1) (演示 + 示例用例)
>

![Tavily MCP 演示](./assets/mcp-demo.gif)

模型上下文协议（MCP）是一个开放标准，它使 AI 系统能够无缝地与各种数据源和工具进行交互，促进安全的双向连接。

由 Anthropic 开发的模型上下文协议（MCP）使像 Claude 这样的 AI 助手能够无缝集成 Tavily 的高级搜索和数据提取功能。这种集成提供了对网络信息的实时访问，具备复杂的过滤选项和特定领域的搜索功能。

Tavily MCP 服务器提供：
- 与 tavily-search 和 tavily-extract 工具的无缝交互
- 通过 tavily-search 工具实现实时网络搜索能力
- 通过 tavily-extract 工具从网页中智能提取数据


## 前提条件 ð§

在开始之前，请确保您已具备以下条件：

- [Tavily API 密钥](https://app.tavily.com/home)
  - 如果您还没有 Tavily API 密钥，可以在此处注册一个免费帐户 [这里](https://app.tavily.com/home)
- [Claude Desktop](https://claude.ai/download) 或 [Cursor](https://cursor.sh)
- [Node.js](https://nodejs.org/) (v20 或更高版本)
  - 您可以通过运行以下命令来验证 Node.js 安装：
    - `node --version`
- [Git](https://git-scm.com/downloads) 安装（仅在使用 Git 安装方法时需要）
  - 在 macOS 上：`brew install git`
  - 在 Linux 上：
    - Debian/Ubuntu: `sudo apt install git`
    - RedHat/CentOS: `sudo yum install git`
  - 在 Windows 上：下载 [Git for Windows](https://git-scm.com/download/win)

## Tavily MCP 服务器安装 â¡

### 使用 NPX 运行

```bash
npx -y tavily-mcp@0.1.4
```


### 通过 Smithery 安装

要通过 [Smithery](https://smithery.ai/server/@tavily-ai/tavily-mcp) 自动为 Claude Desktop 安装 Tavily MCP 服务器：

```bash
npx -y @smithery/cli install @tavily-ai/tavily-mcp --client claude
```


虽然您可以单独启动服务器，但这在孤立状态下并不是特别有用。相反，您应该将其集成到 MCP 客户端中。下面是如何配置 Claude Desktop 应用程序以与 tavily-mcp 服务器一起工作的示例。


## 配置 MCP 客户端 âï¸

此仓库将解释如何配置 [Cursor](https://cursor.sh) 和 [Claude Desktop](https://claude.ai/desktop) 以与 tavily-mcp 服务器一起工作。

### 配置 Cline ð¤

在 Cline 中设置 Tavily MCP 服务器最简单的方法是通过市场一键安装：

1. 在 VS Code 中打开 Cline
2. 点击侧边栏中的 Cline 图标
3. 导航到 MCP Servers 标签（4个方块图标）
4. 搜索 Tavily 并点击 安装
5. 当提示时，输入你的 Tavily API 密钥

或者，你可以手动在 Cline 中设置 Tavily MCP 服务器：

1. 打开 Cline MCP 设置文件：

   ### 对于 macOS：
   ```bash
   # 使用 Visual Studio Code
   code ~/Library/Application\\ Support/Code/User/globalStorage/saoudrizwan.claude-dev/settings/cline_mcp_settings.json

   # 或者使用 TextEdit
   open -e ~/Library/Application\\ Support/Code/User/globalStorage/saoudrizwan.claude-dev/settings/cline_mcp_settings.json
   ```

   ### 对于 Windows：
   ```bash
   code %APPDATA%\\Code\\User\\globalStorage\\saoudrizwan.claude-dev\\settings\\cline_mcp_settings.json
   ```

2. 将 Tavily 服务器配置添加到文件中：

   将 `your-api-key-here` 替换为你的实际 [Tavily API 密钥](https://tavily.com/api-keys)。

   ```json
   {
     mcpServers: {
       tavily-mcp: {
         command: npx,
         args: [-y, tavily-mcp@0.1.4],
         env: {
           TAVILY_API_KEY: your-api-key-here
         },
         disabled: false,
         autoApprove: []
       }
     }
   }
   ```

3. 保存文件。如果 Cline 已经运行，请重启它。

4. 使用 Cline 时，你现在可以访问 Tavily MCP 工具了。你可以在对话中直接要求 Cline 使用 tavily-search 和 tavily-extract 工具。

### 配置 Cursor ð¥ï¸

> **注意**：需要 Cursor 版本 0.45.6 或更高版本

要在 Cursor 中设置 Tavily MCP 服务器：

1. 打开 Cursor 设置
2. 导航到 Features > MCP Servers
3. 点击 + Add New MCP Server 按钮
4. 填写以下信息：
   - **名称**：输入服务器的昵称（例如：tavily-mcp）
   - **类型**：选择 command 作为类型
   - **命令**：输入运行服务器的命令：
     ```bash
     env TAVILY_API_KEY=your-api-key npx -y tavily-mcp@0.1.4
     ```
     > **重要**：将 `your-api-key` 替换为你的 Tavily API 密钥。你可以在 [app.tavily.com/home](https://app.tavily.com/home) 获取一个。

添加服务器后，它应该会出现在 MCP 服务器列表中。可能需要手动点击 MCP 服务器右上角的刷新按钮来填充工具列表。

Composer Agent 会根据您的查询自动使用 Tavily MCP 工具。最好通过描述您想做的事情来明确请求使用这些工具（例如，“使用 tavily-search 搜索有关 AI 的最新新闻”）。在 Mac 上，按 command + L 打开聊天界面，在屏幕顶部选择 composer 选项，然后在提交按钮旁边选择 agent，并在准备就绪时提交查询。

![Cursor Interface Example](./assets/cursor-reference.png)

### 配置 Claude 桌面应用程序 ð¥ï¸
### 对于 macOS：

```bash
# Create the config file if it doesn''t exist
touch $HOME/Library/Application Support/Claude/claude_desktop_config.json

# Opens the config file in TextEdit
open -e $HOME/Library/Application Support/Claude/claude_desktop_config.json

# Alternative method using Visual Studio Code (requires VS Code to be installed)
code $HOME/Library/Application Support/Claude/claude_desktop_config.json
```


### 对于 Windows：
```bash
code %APPDATA%\\Claude\\claude_desktop_config.json
```


### 添加 Tavily 服务器配置：

将 `your-api-key-here` 替换为您实际的 [Tavily API 密钥](https://tavily.com/api-keys)。

```json
{
  mcpServers: {
    tavily-mcp: {
      command: npx,
      args: [-y, tavily-mcp@0.1.2],
      env: {
        TAVILY_API_KEY: your-api-key-here
      }
    }
  }
}
```


### 2. Git 安装

1. 克隆仓库：
```bash
git clone https://github.com/tavily-ai/tavily-mcp.git
cd tavily-mcp
```


2. 安装依赖项：
```bash
npm install
```


3. 构建项目：
```bash
npm run build
```


### 配置 Claude 桌面应用程序 âï¸
按照上述[配置 Claude 桌面应用程序](#configuring-the-claude-desktop-app-ï¸)部分中概述的配置步骤操作，使用下面的 JSON 配置。

将 `your-api-key-here` 替换为您实际的 [Tavily API 密钥](https://tavily.com/api-keys)，并将 `/path/to/tavily-mcp` 替换为您系统上克隆仓库的实际路径。

```json
{
  mcpServers: {
    tavily: {
      command: npx,
      args: [/path/to/tavily-mcp/build/index.js],
      env: {
        TAVILY_API_KEY: your-api-key-here
      }
    }
  }
}
```


## 在 Claude 桌面应用程序中的使用 ð¯

安装完成后并配置好 Claude 桌面应用程序后，您必须完全关闭并重新打开 Claude 桌面应用程序才能看到 tavily-mcp 服务器。您应该会在应用程序左下角看到一个锤子图标，表示可用的 MCP 工具，您可以点击锤子图标以查看更多关于 tavily-search 和 tavily-extract 工具的信息。

![Alt text](./assets/claude-desktop-ref.png)

现在 Claude 将能够完全访问 tavily-mcp 服务器，包括 tavily-search 和 tavily-extract 工具。如果您将以下示例插入 Claude 桌面应用程序中，您应该能看到 tavily-mcp 服务器工具的效果。

### Tavily 搜索示例

1. **通用网页搜索**：
```
Can you search for recent developments in quantum computing?
```


2. **新闻搜索**：
```
Search for news articles about AI startups from the last 7 days.
```


3. **特定领域搜索**：
```
Search for climate change research on nature.com and sciencedirect.com
```


### Tavily 提取示例

1. **提取文章内容**：
```
Extract the main content from this article: https://example.com/article
```


### â¨ 结合搜索和提取功能 â¨

您还可以结合使用 tavily-search 和 tavily-extract 工具来执行更复杂的任务。

```
Search for news articles about AI startups from the last 7 days and extract the main content from each article to generate a detailed report.
```


## 故障排除 ð ï¸

### 常见问题

1. **找不到服务器**
   - 通过运行 `npm --verison` 来验证 npm 安装。
   - 通过运行 `code ~/Library/Application\\ Support/Claude/claude_desktop_config.json` 检查 Claude 桌面配置语法。
   - 通过运行 `node --version` 确保正确安装了 Node.js。

2. **NPX 相关问题**
  - 如果遇到与 `npx` 相关的错误，可能需要使用 npx 可执行文件的完整路径。
  - 您可以通过在终端中运行 `which npx` 查找此路径，然后在配置中将 `command: npx` 这一行替换为 `command: /full/path/to/npx`。

3. **API 密钥问题**
   - 确认您的 Tavily API 密钥有效
   - 检查配置中是否正确设置了 API 密钥
   - 确认 API 密钥周围没有空格或引号

## 致谢 â¨

- [Model Context Protocol](https://modelcontextprotocol.io) 提供了 MCP 规范
- [Anthropic](https://anthropic.com) 提供了 Claude Desktop', '{
  "mcpServers": {
    "tavily-mcp": {
      "command": "npx",
      "args": ["-y", "tavily-mcp@0.1.2"],
      "env": {
        "TAVILY_API_KEY": "your-api-key-here"
      }
    }
  }
}', null, 'inner', 'NPX', 0, 'system', 'system', '2025-04-27 21:51:59', 'system', '2025-04-27 21:53:59', 'system', 'https://github.com/tavily-ai/tavily-mcp', 0, 15);
DELETE
FROM ws_mcp_server_def
WHERE ID = '994';
INSERT INTO ws_mcp_server_def (id, server_code, icon, name, name_en, description, description_en, readme, server_config,
                               tools, type, org_type, deleted, tenant_id, dept_code, created_date, created_by_user_id,
                               last_updated_date, last_updated_by_user_id, url, install_times, view_times)
VALUES ('994', 'BaiduMaps', '', '百度地图', null,
        '百度地图提供的MCP Server，包含10个符合MCP协议标准的API接口，涵盖逆地理编码、地点检索、路线规划等。  ', null, '# 百度地图 MCP Server

## 简介

百度地图核心API现已全面兼容MCP协议，是国内首家兼容MCP协议的地图服务商。关于MCP协议，详见MCP官方[文档](https://modelcontextprotocol.io/)。

目前提供的MCP工具列表包含8个核心API接口和MCP协议的对接， 涵盖逆地理编码、地点检索、路线规划等。

依赖`MCP Python SDK`和`MCP Typescript SDK`开发，任意支持MCP协议的智能体助手（如`Claude`、`Cursor`以及`千帆AppBuilder`等）都可以快速接入。

以下会给更出详细的适配说明。

## 工具列表

1. 地理编码 `geocoder_v2`
   - 将地址解析为对应的位置坐标
   - 输入: `address`
   - 输出: `location`


2. 逆地理编码 `reverse_geocoding_v3`
   - 将坐标点转换为对应语义化地址
   - 输入: `location`
   - 输出: `formatted_address`, `uid`, `addressComponent`

3. 地点检索 `place_v2_search`
   - 多种场景的地点（POI）检索，包括城市检索、圆形区域检索
   - 输入:
     - `query` 检索关键词
     - `location` 圆形检索的中心点
     - `radius` 圆形检索的半径
     - `region` 城市检索指定城市
   - 输出: POI列表，包含`name`, `location`, `address`等

4. 地点详情检索 `place_v2_detail`
   - 根据POI的uid，检索POI详情信息
   - 输入: `uid`
   - 输出: POI详情，包含`name`, `location`, `address`, `brand`, `price`等

5. 批量算路 `routematrix_v2_driving`
   - 根据起点和终点坐标，计算所有起终点组合间的路线距离和行驶时间
   - 输入:
     - `origins` 起点经纬度列表
     - `destinations` 终点经纬度列表
     - `mode` 出行类型，可选取值包括 `driving`、`walking`、`riding`，默认使用`driving`
   - 输出: 每条路线的耗时和距离，包含`distance`, `duration`等

6. 路线规划 `directionlite_v1`
   - 根据起终点坐标规划出行路线和耗时，可指定驾车、步行、骑行、公交等出行方式
   - 输入:
     - `origin` 起点经纬度
     - `destination` 终点经纬度
     - `model` 出行类型，可选取值包括 `driving`、`walking`、`riding`、`transit`，默认使用`driving`
   - 输出: 路线详情，包含`steps`, `distance`, `duration`等

7. 天气查询 `weather_v1`
   - 根据行政区划编码查询天气
   - 输入: `district_id` 行政区划编码
   - 输出: 天气信息，包含`temperature`, `weather`, `wind`等

8. IP定位 `location_ip`
   - 根据请求的IP获取当前请求的位置（定位到城市），如果当使用的是IPv6需要申请高级权限
   - 输入: 无
   - 输出: 当前所在城市和城市中点`location`


## 快速开始

使用百度地图MCP Server主要通过两种形式，分别是`Python`和`Typescript`，下面分别介绍。

### 获取AK

在选择两种方法之前，你需要在[百度地图开放平台](https://lbsyun.baidu.com/apiconsole/key)的控制台中创建一个服务端AK，通过AK你才能够调用百度地图API能力。


### Python

#### 搭建Python虚拟环境
我们推荐通过`Python`虚拟环境来运行MCP server，环境搭建的过程参考[MCP官方文档](https://github.com/modelcontextprotocol/python-sdk?tab=readme-ov-file#overview)。

按照[官方流程](https://docs.astral.sh/uv/getting-started/features/)，你会安装`Python`包管理工具`uv`。除此之外，你也可以尝试其他方法（如`Anaconda`）来创建你的`Python`虚拟环境。

通过`uv`添加`mcp`依赖

```bash
uv add "mcp[cli]"
```

验证mcp依赖是否安装成功，执行如下命令
```bash
uv run mcp
```

当出现下图时代表安装成功

![](./img/uv_install_success.png)

通过`uv`安装`python`，最低版本要求为3.11

```bash
uv python install 3.11
```

#### 获取 MCP Server
前往百度地图 Mcp Server 官方[开源仓库](https://github.com/baidu-maps/mcp/tree/main/src/baidu-map/python)下载

#### 配置本地项目
通过`uv`创建一个项目

```bash
uv init baidu_map_mcp_server
```

将`map.py`拷贝到该目录下，通过如下命令测试mcp server是否正常运行

```bash
uv run --with mcp[cli] mcp run {替换成您的路径}/baidu_map_mcp_server/map.py
# 如果是mac，需要加转义符
uv run --with mcp\\[cli\\] mcp run {替换成您的路径}/baidu_map_mcp_server/map.py
```

如果没有报错则MCP Server启动成功

![](./img/mcp_run_success.png)


#### 在Cursor中使用

打开`Cursor`配置，在MCP中添加MCP Server

![](./img/cursor_setting.png)

在文件中添加如下内容后保存

```json
{
  "mcpServers": {
    "baidu-map": {
      "command": "uv",
      "args": [
        "run",
        "--with",
        "mcp[cli]",
        "mcp",
        "run",
        "{替换成您的路径}/baidu_map_mcp_server/map.py"
      ],
      "env": {
        "BAIDU_MAPS_API_KEY": "{您的AK}"
      }
    }
  }
}
```

回到配置，此时百度MCP Server已经启用

![](./img/cursor_run_mcp_success.png)

#### 测试

行程规划：

![](./img/cursor_test_1.png)

![](./img/cursor_test_2.png)

### Typescript接入

打开`Claude for Desktop`的`Setting`，切换到`Developer`，点击`Edit Config`，用任意的IDE打开配置文件。

![](./img/claude_setting.png)



![](./img/claude_setting_developer.png)



将以下配置添加到配置文件中，BAIDU_MAP_API_KEY 是访问百度地图开放平台API的AK，在[此页面](https://lbs.baidu.com/faq/search?id=299&title=677)中申请获取：

```json
{
    "mcpServers": {
        "baidu-map": {
            "command": "npx",
            "args": [
                "-y",
                "@baidumap/mcp-server-baidu-map"
            ],
            "env": {
                "BAIDU_MAP_API_KEY": "xxx"
            }
        }
    }
}
```

重启Claude，此时设置面板已经成功加载了百度地图MCP Server。在软件主界面对话框处可以看到有8个可用的MCP工具，点击可以查看详情。

![](./img/claude_setting_result.png)

![](./img/claude_result.png)

接下来就可以进行提问，验证出行规划小助手的能力了。
![](./img/claude_final_result.png)


#### 通过千帆AppBuilder平台接入

千帆平台接入，目前支持SDK接入或是API接入，通过AppBuilder构建一个应用，每个应用拥有一个独立的app\\_id，在python文件中调用对应的app\\_id，再调用百度地图 Python MCP Tool即可。

模板代码可向下跳转，通过SDK Agent &&地图MCP Server，拿到导航路线及路线信息，并给出出行建议。


##### Agent配置

前往[千帆平台](https://console.bce.baidu.com/ai_apaas/personalSpace/app)，新建一个应用，并发布。

![](./img/appbuilder.png)

将Agent的思考轮数调到6。发布应用。

##### 调用

此代码可以当作模板，以SDK的形式调用千帆平台上已经构建好且已发布的App，再将MCP Server下载至本地，将文件相对路径写入代码即可。

***（注意：使用实际的app_id、token、query、mcp文件）***

```python
import os
import asyncio
import appbuilder
from appbuilder.core.console.appbuilder_client.async_event_handler import (
    AsyncAppBuilderEventHandler,
)
from appbuilder.modelcontextprotocol.client import MCPClient
class MyEventHandler(AsyncAppBuilderEventHandler):
    def __init__(self, mcp_client):
        super().__init__()
        self.mcp_client = mcp_client
    def get_current_weather(self, location=None, unit="摄氏度"):
        return "{} 的温度是 {} {}".format(location, 20, unit)
    async def interrupt(self, run_context, run_response):
        thought = run_context.current_thought
        # 绿色打印
        print("\\033[1;31m", "-> Agent 中间思考: ", thought, "\\033[0m")
        tool_output = []
        for tool_call in run_context.current_tool_calls:
            tool_res = ""
            if tool_call.function.name == "get_current_weather":
                tool_res = self.get_current_weather(**tool_call.function.arguments)
            else:
                print(
                    "\\033[1;32m",
                    "MCP工具名称: {}, MCP参数:{}\\n".format(tool_call.function.name, tool_call.function.arguments),
                    "\\033[0m",
                )
                mcp_server_result = await self.mcp_client.call_tool(
                    tool_call.function.name, tool_call.function.arguments
                )
                print("\\033[1;33m", "MCP结果: {}\\n\\033[0m".format(mcp_server_result))
                for i, content in enumerate(mcp_server_result.content):
                    if content.type == "text":
                        tool_res += mcp_server_result.content[i].text
            tool_output.append(
                {
                    "tool_call_id": tool_call.id,
                    "output": tool_res,
                }
            )
        return tool_output
    async def success(self, run_context, run_response):
        print("\\n\\033[1;34m", "-> Agent 非流式回答: ", run_response.answer, "\\033[0m")
async def agent_run(client, mcp_client, query):
    tools = mcp_client.tools
    conversation_id = await client.create_conversation()
    with await client.run_with_handler(
        conversation_id=conversation_id,
        query=query,
        tools=tools,
        event_handler=MyEventHandler(mcp_client),
    ) as run:
        await run.until_done()
### 用户Token
os.environ["APPBUILDER_TOKEN"] = (
    ""
)
async def main():
    appbuilder.logger.setLoglevel("DEBUG")
    ### 发布的应用ID
    app_id = ""
    appbuilder_client = appbuilder.AsyncAppBuilderClient(app_id)
    mcp_client = MCPClient()

    ### 注意这里的路径为MCP Server文件在本地的相对路径
    await mcp_client.connect_to_server("./<YOUR_FILE_PATH>/map.py")
    print(mcp_client.tools)
    await agent_run(
        appbuilder_client,
        mcp_client,
        ''开车导航从北京到上海'',
    )
    await appbuilder_client.http_client.session.close()
if __name__ == "__main__":
    loop = asyncio.get_event_loop()
    loop.run_until_complete(main())
```

##### 效果

经过Agent自己的思考，通过调用MCPServer 地点检索、地理编码服务、路线规划服务等多个tool，拿到导航路线及路线信息，并给出出行建议。

实际用户请求：***“请为我计划一次北京赏花一日游。尽量给出更舒适的出行安排，当然，也要注意天气状况。”***

##### 思考过程

![](./img/thinking_progress.png)

##### Agent结果

![](./img/agent_result.png)

# 更新日志

| 版本 | 功能说明                   | 更新日期      |
| ---- | -------------------------- | ------------- |
| V1.0 | 百度地图MCP Server正式上线 | 2025年3月21日 |
| V1.1 | 补充`uvx`、`pip`形式的快速接入 | --- |', '{
    "mcpServers": {
        "baidu-map": {
            "command": "npx",
            "args": [
                "-y",
                "@baidumap/mcp-server-baidu-map"
            ],
            "env": {
                "BAIDU_MAP_API_KEY": "xxx"
            }
        }
    }
}', null, 'private', 'NPX', 0, 'system', 'system', '2025-04-22 14:13:20', 'system', '2025-04-22 11:13:24', 'system', 'https://github.com/baidu-maps/mcp', 4, 36);
DELETE
FROM ws_mcp_server_def
WHERE ID = '968';
INSERT INTO ws_mcp_server_def (id, server_code, icon, name, name_en, description, description_en, readme, server_config,
                               tools, type, org_type, deleted, tenant_id, dept_code, created_date, created_by_user_id,
                               last_updated_date, last_updated_by_user_id, url, install_times, view_times)
VALUES ('968', 'Funcun', '', '方寸MCP', 'Funcun',
        'Funcun MCP是方寸智能提供的关于公文检索、政务审校以及公文写作三种场景的MCP 服务。', null, '# 介绍
关于方寸智能关于公文检索、公文审校以及公文写作三种场景的MCP Server说明。
> 公文写作还在开发中，暂未提供
# 功能
- 检索：使用关键字对公文进行检索，获取到与关键字相关的公文。
- 审校：对文章、语句进行审校，可以支持政务审校、法律审校、固定搭配等政务法规相关的审校
', '{
    "mcpServers": {
        "fc-mcp-server": {
            "type": "sse",
            "url": "http://36.111.151.74:10700/sse",
            "headers":{
                "Authorization":"输入您的请求头VALUE"
            }
        }
    }
}', null, 'inner', 'SSE', 0, 'system', 'system', '2025-06-16 05:13:20', 'system', '2025-06-16 11:13:24', 'system', '', 3, 66);
DELETE
FROM ws_mcp_server_def
WHERE ID = '969';
INSERT INTO ws_mcp_server_def (id, server_code, icon, name, name_en, description, description_en, readme, server_config,
                               tools, type, org_type, deleted, tenant_id, dept_code, created_date, created_by_user_id,
                               last_updated_date, last_updated_by_user_id, url, install_times, view_times)
VALUES ('969', 'Baiwang-Tax&Finance&Data', '', '百望财税-数据', 'Baiwang Tax&Finance&Data',
        '为了支持LLM智能体快速连接至百望云SaaS和DaaS平台，访问财税与数据智能驱动的API服务，百望开放平台提供基于MCP Server的API访问能力，支持LLM智能体简化连接，快速集成百望云API。',
        null, '## 简介
为了支持LLM智能体快速连接至百望云SaaS和DaaS平台，访问财税与数据智能驱动的API服务，百望开放平台提供基于MCP Server的API访问能力，支持LLM智能体简化连接，快速集成百望云API。。

## 使用方式
该MCP Server已在云端部署完成，开通后即可前往应用管理模块，编辑应用时添加使用。

## 关键能力
- 快速连接至百望云SaaS和DaaS平台，访问财税与数据智能驱动的API服务；
- 提供面向多场景的财税基础API服务，为多领域业务LLM智能体提供支撑；
- 简单高效，简化连接，快速集成。
- 支持高效通信机制，快速处理大模型 API 的请求和响应，确保低延迟和高吞吐量。
- 采用分布式架构，支持高并发访问，支持负载均衡和故障转移，确保服务的高可用性。

## 使用场景
- 查询企业抬头信息，云抬头。
- 查询商品税收分类编码信息，智能赋码。

## FAQ
Q：百望云财税&数据 MCP 服务是否收费？
A：百望云结合财税专业领域的深耕与运营经验，结合LLM业务发展需要，提供一定期限的免费试用，试用期结束后需要付费。付费标准参考百望云开放平台资费标准。对于长期合作的企业，提供优惠的收费方案和优惠政策。

<br>
Q：百望云支持的MCP客户端类型有哪些？
A：支持任意 MCP 协议的客户端（如：Cursor）方便使用百望云财税&数据 MCP server。支持 MCP（SSE）接入方式，NPX接入方式。

## License
此 MCP 服务器采用 [MIT 许可证}(https://github.com/modelcontextprotocol/servers/tree/main/src/time)。这意味着您可以自由使用、修改和分发该软件，但须遵守 MIT 许可证的条款和条件。
', '{
	"mcpServers": {
		"baiwang-sse": {
			"url": "https://openapi.baiwang.com/mcp/ua/sse?key=您在开放平台上申请的apikey"
		}
	}
}', null, 'inner', 'SSE', 0, 'system', 'system', '2025-06-16 16:13:20', 'system', '2025-06-16 16:13:24', 'system', '', 40, 64);
DELETE
FROM ws_mcp_server_def
WHERE ID = '972';
INSERT INTO ws_mcp_server_def (id, server_code, icon, name, name_en, description, description_en, readme, server_config,
                               tools, type, org_type, deleted, tenant_id, dept_code, created_date, created_by_user_id,
                               last_updated_date, last_updated_by_user_id, url, install_times, view_times)
VALUES ('972', 'Baiwang-Insight', '', '百望睿识', 'Baiwang-Insight',
        '为银行保险机构，财税机构，以及企业，提供全场景覆盖的发票核验服务，提供包括全类型发票核验真伪，全票面信息获取，异常状态提醒，多投风险预警，交易背景核查，发票红冲信息列表等关键业务风险数据。',
        null, '**#****# 简介**

为银行保险机构，财税机构，以及企业，提供全场景覆盖的发票核验服务，提供包括全类型发票核验真伪，全票面信息获取，异常状态提醒，多投风险预警，交易背景核查，发票红冲信息列表等关键业务风险数据。

**## 使用方式**

您需要前往 [百望银税通开放平台](https://open.yinshuitong.com/dist/index.html#/login/phone?redirect=apiCenter)，登录后，可以在 API keys 管理中心，点击新增 API key 获取。

**## 关键能力**

- 全面覆盖
  支持全国范围多种票据类型的查验服务（如增值税专用发票、普通发票等），满足不同场景下的核验需求
- API端口交付，集成便捷
  提供标准化的API接口服务，便于企业快速对接系统，实现核验流程的自动化与智能化
- 广泛的应用场景赋能
  覆盖承兑、贴现、贸易背景审查、内部审计与合规检查等多种业务场景，助力企业提升运营效率和风险防控能力

**## 使用场景**

- 发票核验真伪；
- 全票面信息获取；
- 异常状态提醒；
- 多投风险预警。

**## FAQ**

Q：使用百望睿识服务是否需要付费？
A：百望云结合财税、SAAS专业领域的多年的积累，结合各票据使用方业务发展需要，提供一定期限的免费试用，试用期结束后需要付费。

Q：支持的MCP客户端类型有哪些？
A：支持任意 MCP 协议的客户端（如：Cursor）。

Q：支持哪些接入方式？
A：目前仅支持 Node.js I/O 模式接入 MCP 服务（请确保已安装 Node.js，并检查本地 Node.js 版本是否为 v22.12.0 或更高版本）。
', '{
	"mcpServers": {
		"bw-invoic-sse": {
			"url": "https://smartsk-api.baiwang.com/mcp/ua/sse?key=您在开放平台上申请的apikey"
		}
	}
}', null, 'inner', 'SSE', 0, 'system', 'system', '2025-06-16 16:13:22', 'system', '2025-06-16 16:13:26', 'system', '', 4, 68);
DELETE
FROM ws_mcp_server_def
WHERE ID = '973';
INSERT INTO ws_mcp_server_def (id, server_code, icon, name, name_en, description, description_en, readme, server_config,
                               tools, type, org_type, deleted, tenant_id, dept_code, created_date, created_by_user_id,
                               last_updated_date, last_updated_by_user_id, url, install_times, view_times)
VALUES ('973', 'Clouddocs-MCP', '', '云文档MCP', 'Clouddocs-MCP',
        '基于MCP(Model Context Protocol)的云文档工具,支持文档创建和追加内容。', null, '## 使用方式
一个基于MCP(Model Context Protocol)的云文档工具，该工具支持以下功能:
1. document_create: 基于用户的要求，创建文档，添加标题和内容
2. document_append: 基于用户要求，在已创建的文档下追加内容

## 注意
注意: 在配置智能体时，如果使用云文档MCP, 需要在该链接下配置API-KEY并带入变量中，才可以正常使用工具:
', '{
    "mcpServers": {
        "clouddocs-sse": {
            "url": ""
        }
    }
}', null, 'inner', 'SSE', 0, 'system', 'system', '2025-06-17 16:14:20', 'system', '2025-06-16 17:14:24', 'system', '', 34, 315);

DELETE
FROM ws_mcp_service_def
WHERE id = '00df77e301b94c3ea9dfcae110cf379a';

INSERT INTO ws_mcp_service_def (id, name, name_en, description, description_en, fc_instance_url, fc_instance_id,
                                fc_instance_status, fc_region, apig_group_id, apig_instance_id, deleted, tenant_id,
                                dept_code, created_date, created_by_user_id, last_updated_date, last_updated_by_user_id,
                                readme, server_config, tools, deploy_type, org_type, function_name, server_id,
                                function_urn, function_application_name, domain_id, unique_code, project_id,
                                workspace_id)
VALUES ('00df77e301b94c3ea9dfcae110cf379a', '云文档MCPtesttesttest', 'Clouddocs-MCP',
        '基于MCP(Model Context Protocol)的云文档工具,支持文档创建和追加内容。', null,
        'AAAAAgAAAAAAAAAAAAAAAQAAAAdpkliySfoJ8/YtCCei+zWDLb3rTxbJDovm4pgTrHdjUQEAAAEAAAAAAAAAcDJW5GCstPxMmfZRFXunx0wBJosrz441Wl83NOXDu3UncWukI3QkWGp9W9Wb0WrqlalQfJ2bbWipoSbBdxhzaZea/sVXYGFbEpU9YsF6dEi21qkZ4loKXWLo7cN0WduazyZAqlO6iALjfrhy2bI6ec4=',
        null, 'success', null, null, null, 0, 'system', 'system', '2025-08-08 11:42:22', '2', '2025-08-08 11:42:22',
        '2', '## 使用方式
一个基于MCP(Model Context Protocol)的云文档工具，该工具支持以下功能:
1. document_create: 基于用户的要求，创建文档，添加标题和内容
2. document_append: 基于用户要求，在已创建的文档下追加内容

## 注意
注意: 在配置智能体时，如果使用云文档MCP, 需要在该链接下配置API-KEY并带入变量中，才可以正常使用工具:
', '{
  "mcpServers" : {
    "clouddocs-sse" : {
      "url" : ""
    }
  }
}', null, 'sse', 'SSE', null, '73', null, '云文档MCPtesttesttest-0s3UmqLH-AIAE', null, 'b2db3495cb024b6aa4508b775ba30cf4', 'test_project_id', 'default');

DELETE
FROM ws_mcp_service_def
WHERE id = '00df77e301b94c3ea9dfcae110cf379b';
INSERT INTO ws_mcp_service_def (id, name, name_en, description, description_en, fc_instance_url, fc_instance_id,
                                fc_instance_status, fc_region, apig_group_id, apig_instance_id, deleted, tenant_id,
                                dept_code, created_date, created_by_user_id, last_updated_date, last_updated_by_user_id,
                                readme, server_config, tools, deploy_type, org_type, function_name, server_id,
                                function_urn, function_application_name, domain_id, unique_code, project_id,
                                workspace_id, auth_type)
VALUES ('00df77e301b94c3ea9dfcae110cf379b', '空白模板233', 'Empty Template', '基于空白模板快速构建自己的业务逻辑233',
        'Base on empty template to create mcp service', '', null, 'success', null, null, null, 0, 'system', 'system',
        '2025-08-07 21:08:00', 'system', '2025-08-07 21:11:57', '2', null, '{
  "mcpServers" : {
    "example-sse" : {
      "url" : "http://xxxx:8080/sse"
    }
  }
}', '[{"name":"createCalendar","description":"日历，日程，提醒等类似于备忘录类事件新增","inputSchema":{"type":"object","properties":{"content":{"type":"string","description":"内容"},"summary":{"type":"string","description":"综述 没有特别注释时和内容字段一样"},"receiverUserList":{"type":"array","items":{"type":"string"},"description":"接受者ID集合，最大容量500"},"reminder":{"type":"object","properties":{"minutes":{"type":"number","description":"距开始时多久进行提醒(单位:分钟)"}},"required":["minutes"],"additionalProperties":false},"startTime":{"type":"string","description":"日历开始时间，GMT+0800（时区）时间字符串，格式为: YYYY-MM-DD HH:MM 如果没有指定开始时间或填空串，则表示日历5分钟后开始"},"endTime":{"type":"string","description":"日历结束时间，GMT+0800（时区）时间字符串，格式为: YYYY-MM-DD HH:MM 如果没有指定开始时间或填空串，则表示开始时间往后30分钟"},"location":{"type":"string","description":"地址"},"recurrence":{"type":"object","properties":{"frequency":{"type":"string","description":"重复基准， ''DAILY'', ''WEEKLY'', ''MONTHLY'', ''YEARLY'' "},"count":{"type":"number","description":"重复次数"},"until":{"type":"string","description":"设置截止日期，不可与count同时出现, 格式为：YYYY-MM-DD HH:MM"},"days":{"type":"array","items":{"type":"string"},"description":"当frequency为MONTHLY or YEARLY时，表示周中的哪些天。例如   [{''ordwk'':3, ''weekday'': ''SU''}] 表示第三个周日"},"interval":{"type":"number","description":"依赖freq，重复周期。如：frequency=DAILY;interval=8. 每隔8天重复一次"},"monthdays":{"type":"array","items":{"type":"number"},"description":"单月内，哪些天"},"months":{"type":"array","items":{"type":"number"},"description":"一年内，哪些月份"}},"required":["frequency","count","until","days","interval","monthdays","months"],"additionalProperties":false,"description":"日历重复规则，示例：\"recurrence\": {     \"frequency\": \"DAILY\",     \"interval\":2,     \"count\":10 } 表示：每隔一天，共发生10次。  改字段在创建普通日程，或者没有明确表示需要重复的日程时不填"}},"required":["content","summary","receiverUserList","startTime","endTime"],"additionalProperties":false}},{"name":"queryCalendar","description":"查询用户指定时间段的日程，日历","inputSchema":{"type":"object","properties":{"userId":{"type":"string","description":"当前用户的userid"},"startTime":{"type":"string","description":"用户查询日历的开始时间，GMT+0800（时区）时间字符串，格式为: YYYY-MM-DD HH:MM 如果没有指定开始时间或填空串, 则取当前时间"},"endTime":{"type":"string","description":"用户查询日历的结束时间，GMT+0800（时区）时间字符串，格式为: YYYY-MM-DD HH:MM 如果没有指定开始时间或填空串, 则取当前时间的后一天"}},"required":["userId","startTime","endTime"],"additionalProperties":false}},{"name":"updateCalendar","description":"调整和修改指定日历，通过时间或者名称匹配日历的calUid和上下文中查询出来的结果作为基础进行更新","inputSchema":{"type":"object","properties":{"calUid":{"type":"string","description":"待更新日历的日历id"},"content":{"type":"string","description":"内容"},"summary":{"type":"string","description":"综述，非必填"},"receiverUserList":{"type":"array","items":{"type":"string"},"description":"接受者ID集合，最大容量500, 如果接收人为空 则从上下文中找到查询用户的userId"},"reminder":{"type":"object","properties":{"minutes":{"type":"number","description":"距开始时多久进行提醒(单位:分钟)"}},"required":["minutes"],"additionalProperties":false},"startTime":{"type":"string","description":"日历开始时间，GMT+0800（时区）时间字符串，格式为: YYYY-MM-DD HH:MM 如果没有指定开始时间或填空串，则表示日历5分钟后开始"},"endTime":{"type":"string","description":"日历结束时间，GMT+0800（时区）时间字符串，格式为: YYYY-MM-DD HH:MM 如果没有指定开始时间或填空串，则表示开始时间往后30分钟"},"location":{"type":"string","description":"地址"},"recurrence":{"type":"object","properties":{"frequency":{"type":"string","description":"重复基准， ''DAILY'', ''WEEKLY'', ''MONTHLY'', ''YEARLY'' "},"count":{"type":"number","description":"重复次数"},"until":{"type":"string","description":"设置截止日期，不可与count同时出现, 格式为：YYYY-MM-DD HH:MM"},"days":{"type":"array","items":{"type":"string"},"description":"当frequency为MONTHLY or YEARLY时，表示周中的哪些天。例如   [{''ordwk'':3, ''weekday'': ''SU''}] 表示第三个周日"},"interval":{"type":"number","description":"依赖freq，重复周期。如：frequency=DAILY;interval=8. 每隔8天重复一次"},"monthdays":{"type":"array","items":{"type":"number"},"description":"单月内，哪些天"},"months":{"type":"array","items":{"type":"number"},"description":"一年内，哪些月份"}},"required":["frequency","count","until","days","interval","monthdays","months"],"additionalProperties":false,"description":"日历重复规则，示例：\"recurrence\": {     \"frequency\": \"DAILY\",     \"interval\":2,     \"count\":10 } 表示：每隔一天，共发生10次。  改字段在创建普通日程，或者没有明确表示需要重复的日程时不填"}},"required":["calUid","content","summary","receiverUserList","reminder","startTime","endTime","location"],"additionalProperties":false}},{"name":"deleteCalendar","description":"删除日历服务","inputSchema":{"type":"object","properties":{"calUid":{"type":"string","description":"待更新日历的日历id"},"receiverUserList":{"type":"array","items":{"type":"string"},"description":"接受者ID集合，最大容量500, 需要删除的人员列表"}},"required":["calUid","receiverUserList"],"additionalProperties":false}},{"name":"queryFreebusy","description":"查询用户闲忙和日程安排服务，用于查询用户在指定时间内是否有会议和日程的冲突","inputSchema":{"type":"object","properties":{"users":{"type":"array","items":{"type":"string"},"description":"需要查询人员列表，最大100"},"startTime":{"type":"string","description":"日历查询开始时间，GMT+0800（时区）时间字符串，格式为: YYYY-MM-DD HH:MM 如果没有指定开始时间或填空串，则表示当天开始查询"},"endTime":{"type":"string","description":"日历查询结束时间，GMT+0800（时区）时间字符串，格式为: YYYY-MM-DD HH:MM 如果没有指定开始时间或填空串，则表示开始时间往后1天"},"calUid":{"type":"string","description":"日历id,用于根据uid排除查询某日程"}},"required":["users","startTime","endTime"],"additionalProperties":false}}]',
        'stdio', 'SSE', null, '1', null, '空白模板233-g6dypa9D-AIAE', null, '533c8eef0d55426d9f58ed4298f3a88c', 'test_project_id', 'default', null);

DELETE
FROM ws_mcp_server_rating
WHERE rating_id = 1;
INSERT INTO ws_mcp_server_rating (rating_id, server_id, tenant_id, score, created_date, updated_date)
VALUES (1, '67', '1348778111', 10, '2025-06-11 09:47:27', '2025-06-11 09:47:27');
DELETE
FROM ws_mcp_server_rating
WHERE rating_id = 2;
INSERT INTO ws_mcp_server_rating (rating_id, server_id, tenant_id, score, created_date, updated_date)
VALUES (2, '67', '42126902', 6, '2025-06-12 22:08:42', '2025-06-12 22:08:42');
DELETE
FROM ws_mcp_server_rating
WHERE rating_id = 3;
INSERT INTO ws_mcp_server_rating (rating_id, server_id, tenant_id, score, created_date, updated_date)
VALUES (3, '26', '42126902', 9, '2025-06-13 00:18:36', '2025-06-13 00:18:36');
DELETE
FROM ws_mcp_server_rating
WHERE rating_id = 4;
INSERT INTO ws_mcp_server_rating (rating_id, server_id, tenant_id, score, created_date, updated_date)
VALUES (4, '69', '9190086000001247361', 8, '2025-06-17 14:38:08', '2025-06-17 14:38:08');
DELETE
FROM ws_mcp_server_rating
WHERE rating_id = 5;
INSERT INTO ws_mcp_server_rating (rating_id, server_id, tenant_id, score, created_date, updated_date)
VALUES (5, '73', '264416321', 10, '2025-07-25 15:23:43', '2025-07-25 15:25:29');
DELETE
FROM ws_mcp_server_rating
WHERE rating_id = 7;
INSERT INTO ws_mcp_server_rating (rating_id, server_id, tenant_id, score, created_date, updated_date)
VALUES (7, '72', '850990167', 1, '2025-08-06 10:43:11', '2025-08-06 10:44:03');
DELETE
FROM ws_mcp_server_rating
WHERE rating_id = 10;
INSERT INTO ws_mcp_server_rating (rating_id, server_id, tenant_id, score, created_date, updated_date)
VALUES (10, '71', '1231753558', 2, '2025-08-06 14:12:13', '2025-08-06 14:12:13');


DELETE
FROM ws_mcp_service_def
WHERE id = '1c8a6f8868c34d61ae2e0549a33f0a9c';
INSERT INTO ws_mcp_service_def (id, name, name_en, description, description_en, fc_instance_url, fc_instance_id,
                                fc_instance_status, fc_region, apig_group_id, apig_instance_id, deleted, tenant_id,
                                dept_code, created_date, created_by_user_id, last_updated_date, last_updated_by_user_id,
                                readme, server_config, tools, deploy_type, org_type, function_name, server_id,
                                function_urn, function_application_name, domain_id, unique_code, project_id,
                                workspace_id)
VALUES ('1c8a6f8868c34d61ae2e0549a33f0a9c', '空白模板javatest', 'Empty Template', '基于空白模板快速构建自己的业务逻辑',
        'Base on empty template to create mcp service', '', null, 'success', null, null, null, 0, 'system', 'system',
        '2025-08-07 19:47:12', '2', '2025-08-07 20:36:41', '2', null, '{
  "mcpServers" : {
    "example-sse" : {
      "url" : "http://xxxx:8080/sse"
    }
  }
}', '[{"name":"queryTestCaseList","description":"查询一个Agent智能体提示词所有的测试用例列表，包含了每个测试用例的详细信息。注意：获取到数据后应该用表格呈现。","inputSchema":{"type":"object","properties":{"promptId":{"type":"string","description":"提示词的ID。注意：不要捏造ID，如果你不知道ID，应该询问用户修改哪一个提示词。"}},"required":["promptId"],"additionalProperties":false}},{"name":"deleteTestTaskById","description":"删除一个测试历史任务。","inputSchema":{"type":"object","properties":{"testTaskId":{"type":"string","description":"测试任务的ID。"}},"required":["testTaskId"],"additionalProperties":false}},{"name":"saveTestResult","description":"测试结果保存，用来保存测试任务的结果，你应该在完成所有的用户要求的测试任务后立即调用此工具保存测试结果。","inputSchema":{"type":"object","properties":{"testTaskResult":{"type":"object","properties":{"prompt_content":{"type":"string","description":"Agent智能体的提示词内容"},"prompt_id":{"type":"string","description":"提示词的ID。注意：不要捏造ID，如果你不知道ID，应该询问用户修改哪一个提示词。"},"prompt_type":{"type":"string","description":"提示词的类型。"},"prompt_version":{"type":"integer","format":"int32","description":"提示词版本号。"},"test_case_result_list":{"description":"本次的所有测试用例的测试结果。","type":"array","items":{"type":"object","properties":{"score":{"type":"integer","format":"int32","description":"根据测试的真实结果testCaseOutputActualValue和期望值testCaseOutputExpectedValue，对本次测试进行打分。打分规则：1、如果提示词的类型是function，testCaseOutputActualValue和testCaseOutputExpectedValue完全相等，则是满分10分；否则是0分。2、如果提示词的类型是common，请结合原始提示词、测试用例的原始信息对本次测试结果进行综合评价打分，如果输出包含了所有期望的要点则满足10分，包含大部分期望要点得6到9分，包含一半期望要点得5分，包含期望要点较少得1~4分，输出很差不包含任何期望的要点得0分。"},"test_case_id":{"type":"string","description":"测试用例的ID。"},"test_case_input":{"type":"string","description":"测试用例的输入。"},"test_case_name":{"type":"string","description":"测试用例名称。注意：相同提示词的测试用例名称不能重名。"},"test_case_output_actual_value":{"type":"string","description":"测试的真实输出。"},"test_case_output_expected_value":{"type":"string","description":"测试的期望输出。"},"test_review":{"type":"string","description":"根据测试的真实结果和期望值，对本次测试进行评价。"}},"required":["score","test_case_id","test_case_input","test_case_name","test_case_output_actual_value","test_case_output_expected_value","test_review"],"description":"本次的所有测试用例的测试结果。"}}},"required":["prompt_content","prompt_id","prompt_type","prompt_version","test_case_result_list"],"description":"执行测试后返回的测试结果集合。"}},"required":["testTaskResult"],"additionalProperties":false}},{"name":"modifyPromptById","description":"修改一个Agent智能体提示词的内容。","inputSchema":{"type":"object","properties":{"promptId":{"type":"string","description":"提示词的ID。注意：不要捏造ID，如果你不知道ID，应该询问用户修改哪一个提示词。"},"promptContent":{"type":"string","description":"Agent智能体的提示词内容。注意：提示词应该是结构化的，使用markdown格式，要有清晰准确的角色、任务、要求、示例等说明，确保Agent出色地按照要求完成任务。"}},"required":["promptId","promptContent"],"additionalProperties":false}},{"name":"testPrompt","description":"执行一次测试Agent智能体提示词的测试用例，一次支持执行多个测试用例，返回测试的输出结果。注意：1、你应该先获取到被测试提示词的原始信息，以及测试用例的原始信息，然后再使用此工具进行测试获取测试结果。2、此工具只是执行测试获取测试结果，当完成所有的用户要求的测试任务后你应该立即调用测试结果保存工具，把所有的测试结果进行保存，否则用户将无法看到测试结果。3、最终结果可以用表格呈现。","inputSchema":{"type":"object","properties":{"testTaskInfo":{"type":"object","properties":{"prompt_content":{"type":"string","description":"待测试的Agent智能体提示词的原始内容，你需要先从工具获取提示词内容，不要捏造提示词内容；当使用此工具的时候，你应该把这个提示词的内容临时作为你的系统提示词。"},"prompt_id":{"type":"string","description":"提示词的ID。注意：不要捏造ID，如果你不知道ID，应该询问用户修改哪一个提示词。"},"prompt_type":{"type":"string","description":"提示词的类型。"},"prompt_version":{"type":"integer","format":"int32","description":"提示词版本号。"},"test_case_info_list":{"description":"被测试的测试用例的信息集合。","type":"array","items":{"type":"object","properties":{"test_caseId":{"type":"string","description":"测试用例的ID。"},"test_case_input":{"type":"string","description":"测试用例的原始输入，你需要先从工具获取测试用例的输入，不要捏造输入；当使用此工具的时候，你应该把测试用例的输入临时作为用户的输入。"},"test_case_name":{"type":"string","description":"测试用例名称。注意：相同提示词的测试用例名称不能重名。"},"test_case_predicted_output":{"type":"string","description":"你对前测试用例的预测输出，不能为空，需要动态生成；当使用此工具的时候，你应该根据临时的系统提示词promptContent和临时的用户的输入testCaseInput，分析并生成对应的预测输出，并设置到此参数上。"}},"required":["test_caseId","test_case_input","test_case_name","test_case_predicted_output"],"description":"被测试的测试用例的信息集合。"}}},"required":["prompt_content","prompt_id","prompt_type","prompt_version","test_case_info_list"],"description":"测试任务的提示词和测试用例信息。"}},"required":["testTaskInfo"],"additionalProperties":false}},{"name":"queryPromptById","description":"查询一个Agent智能体提示词的详细信息。","inputSchema":{"type":"object","properties":{"promptId":{"type":"string","description":"提示词的ID。注意：不要捏造ID，如果你不知道ID，应该询问用户修改哪一个提示词。"}},"required":["promptId"],"additionalProperties":false}},{"name":"queryTestTaskList","description":"查询一个Agent智能体提示词所有的测试历史任务列表，包含了每个测试任务的详细信息。注意：获取到数据后应该用表格呈现。","inputSchema":{"type":"object","properties":{"promptId":{"type":"string","description":"提示词的ID。注意：不要捏造ID，如果你不知道ID，应该询问用户修改哪一个提示词。"}},"required":["promptId"],"additionalProperties":false}},{"name":"createPrompt","description":"为一个Agent智能体创建一个提示词。","inputSchema":{"type":"object","properties":{"promptName":{"type":"string","description":"提示词名称。注意：提示词的名称不能重名。"},"promptContent":{"type":"string","description":"Agent智能体的提示词内容。注意：提示词应该是结构化的，使用markdown格式，要有清晰准确的角色、任务、要求、示例等说明，确保Agent出色地按照要求完成任务。"},"type":{"type":"string","description":"提示词的类型，取值范围是：function、common。解释说明：function表示函数模式，需要Agent根据提示词输出固定范围的值（比如输出：Y或N、存在或不存在、合法或不合法等有限的固定序列值）；common表示通用模式，输出内容是不固定的。"}},"required":["promptName","promptContent","type"],"additionalProperties":false}},{"name":"queryPromptList","description":"查询所有的Agent智能体提示词列表。注意：获取到数据后应该用表格呈现。","inputSchema":{"type":"object","properties":{},"required":[],"additionalProperties":false}},{"name":"deleteTestCaseById","description":"删除一个测试用例。","inputSchema":{"type":"object","properties":{"testCaseId":{"type":"string","description":"测试用例的ID。"}},"required":["testCaseId"],"additionalProperties":false}},{"name":"modifyTestCase","description":"修改一个测试用例的输入和期望输出。","inputSchema":{"type":"object","properties":{"testCaseId":{"type":"string","description":"测试用例的ID。"},"testCaseInput":{"type":"string","description":"测试用例的输入。"},"testCaseOutput":{"type":"string","description":"测试用例的期望输出。"}},"required":["testCaseId","testCaseInput","testCaseOutput"],"additionalProperties":false}},{"name":"queryTestCaseById","description":"查询一个测试用例的详细信息。","inputSchema":{"type":"object","properties":{"testCaseId":{"type":"string","description":"提示词的ID。注意：不要捏造ID，如果你不知道ID，应该询问用户修改哪一个提示词。"}},"required":["testCaseId"],"additionalProperties":false}},{"name":"queryTestTaskById","description":"查询一个测试历史任务的详细信息。","inputSchema":{"type":"object","properties":{"testTaskId":{"type":"string","description":"测试任务的ID。"}},"required":["testTaskId"],"additionalProperties":false}},{"name":"deletePromptById","description":"删除一个Agent智能体提示词。","inputSchema":{"type":"object","properties":{"promptId":{"type":"string","description":"提示词的ID。注意：不要捏造ID，如果你不知道ID，应该询问用户修改哪一个提示词。"}},"required":["promptId"],"additionalProperties":false}},{"name":"createTestCaseForPrompt","description":"给一个Agent智能体提示词创建一个测试用例。","inputSchema":{"type":"object","properties":{"promptId":{"type":"string","description":"提示词的ID。注意：不要捏造ID，如果你不知道ID，应该询问用户修改哪一个提示词。"},"testCaseName":{"type":"string","description":"测试用例名称。注意：相同提示词的测试用例名称不能重名。"},"testCaseInput":{"type":"string","description":"测试用例的输入。"},"testCaseOutput":{"type":"string","description":"测试用例的期望输出。"}},"required":["promptId","testCaseName","testCaseInput","testCaseOutput"],"additionalProperties":false}}]', 'sse', 'SSE', null, '1', null, '空白模板javatest-G0bOtU2y-AIAE', null, 'dc2aa96a75544a91b9227ace821ffc7d', 'test_project_id', 'default');
DELETE
FROM ws_mcp_service_def
WHERE id = '68345ccb7daf466199dc14eff7992af0';
INSERT INTO ws_mcp_service_def (id, name, name_en, description, description_en, fc_instance_url, fc_instance_id,
                                fc_instance_status, fc_region, apig_group_id, apig_instance_id, deleted, tenant_id,
                                dept_code, created_date, created_by_user_id, last_updated_date, last_updated_by_user_id,
                                readme, server_config, tools, deploy_type, org_type, function_name, server_id,
                                function_urn, function_application_name, domain_id, unique_code, project_id,
                                workspace_id, auth_type)
VALUES ('68345ccb7daf466199dc14eff7992af0', '空白模板233', 'Empty Template', '基于空白模板快速构建自己的业务逻辑233',
        'Base on empty template to create mcp service', '', null, 'success', null, null, null, 0, 'system', 'system',
        '2025-08-07 21:08:00', 'system', '2025-08-07 21:11:57', '2', null, '{
  "mcpServers" : {
    "example-sse" : {
      "url" : "http://xxxx:8080/sse"
    },"headers":{
                "Authorization":"输入您的请求头VALUE"
            }
  }
}', '[{"name":"createCalendar","description":"日历，日程，提醒等类似于备忘录类事件新增","inputSchema":{"type":"object","properties":{"content":{"type":"string","description":"内容"},"summary":{"type":"string","description":"综述 没有特别注释时和内容字段一样"},"receiverUserList":{"type":"array","items":{"type":"string"},"description":"接受者ID集合，最大容量500"},"reminder":{"type":"object","properties":{"minutes":{"type":"number","description":"距开始时多久进行提醒(单位:分钟)"}},"required":["minutes"],"additionalProperties":false},"startTime":{"type":"string","description":"日历开始时间，GMT+0800（时区）时间字符串，格式为: YYYY-MM-DD HH:MM 如果没有指定开始时间或填空串，则表示日历5分钟后开始"},"endTime":{"type":"string","description":"日历结束时间，GMT+0800（时区）时间字符串，格式为: YYYY-MM-DD HH:MM 如果没有指定开始时间或填空串，则表示开始时间往后30分钟"},"location":{"type":"string","description":"地址"},"recurrence":{"type":"object","properties":{"frequency":{"type":"string","description":"重复基准， ''DAILY'', ''WEEKLY'', ''MONTHLY'', ''YEARLY'' "},"count":{"type":"number","description":"重复次数"},"until":{"type":"string","description":"设置截止日期，不可与count同时出现, 格式为：YYYY-MM-DD HH:MM"},"days":{"type":"array","items":{"type":"string"},"description":"当frequency为MONTHLY or YEARLY时，表示周中的哪些天。例如   [{''ordwk'':3, ''weekday'': ''SU''}] 表示第三个周日"},"interval":{"type":"number","description":"依赖freq，重复周期。如：frequency=DAILY;interval=8. 每隔8天重复一次"},"monthdays":{"type":"array","items":{"type":"number"},"description":"单月内，哪些天"},"months":{"type":"array","items":{"type":"number"},"description":"一年内，哪些月份"}},"required":["frequency","count","until","days","interval","monthdays","months"],"additionalProperties":false,"description":"日历重复规则，示例：\"recurrence\": {     \"frequency\": \"DAILY\",     \"interval\":2,     \"count\":10 } 表示：每隔一天，共发生10次。  改字段在创建普通日程，或者没有明确表示需要重复的日程时不填"}},"required":["content","summary","receiverUserList","startTime","endTime"],"additionalProperties":false}},{"name":"queryCalendar","description":"查询用户指定时间段的日程，日历","inputSchema":{"type":"object","properties":{"userId":{"type":"string","description":"当前用户的userid"},"startTime":{"type":"string","description":"用户查询日历的开始时间，GMT+0800（时区）时间字符串，格式为: YYYY-MM-DD HH:MM 如果没有指定开始时间或填空串, 则取当前时间"},"endTime":{"type":"string","description":"用户查询日历的结束时间，GMT+0800（时区）时间字符串，格式为: YYYY-MM-DD HH:MM 如果没有指定开始时间或填空串, 则取当前时间的后一天"}},"required":["userId","startTime","endTime"],"additionalProperties":false}},{"name":"updateCalendar","description":"调整和修改指定日历，通过时间或者名称匹配日历的calUid和上下文中查询出来的结果作为基础进行更新","inputSchema":{"type":"object","properties":{"calUid":{"type":"string","description":"待更新日历的日历id"},"content":{"type":"string","description":"内容"},"summary":{"type":"string","description":"综述，非必填"},"receiverUserList":{"type":"array","items":{"type":"string"},"description":"接受者ID集合，最大容量500, 如果接收人为空 则从上下文中找到查询用户的userId"},"reminder":{"type":"object","properties":{"minutes":{"type":"number","description":"距开始时多久进行提醒(单位:分钟)"}},"required":["minutes"],"additionalProperties":false},"startTime":{"type":"string","description":"日历开始时间，GMT+0800（时区）时间字符串，格式为: YYYY-MM-DD HH:MM 如果没有指定开始时间或填空串，则表示日历5分钟后开始"},"endTime":{"type":"string","description":"日历结束时间，GMT+0800（时区）时间字符串，格式为: YYYY-MM-DD HH:MM 如果没有指定开始时间或填空串，则表示开始时间往后30分钟"},"location":{"type":"string","description":"地址"},"recurrence":{"type":"object","properties":{"frequency":{"type":"string","description":"重复基准， ''DAILY'', ''WEEKLY'', ''MONTHLY'', ''YEARLY'' "},"count":{"type":"number","description":"重复次数"},"until":{"type":"string","description":"设置截止日期，不可与count同时出现, 格式为：YYYY-MM-DD HH:MM"},"days":{"type":"array","items":{"type":"string"},"description":"当frequency为MONTHLY or YEARLY时，表示周中的哪些天。例如   [{''ordwk'':3, ''weekday'': ''SU''}] 表示第三个周日"},"interval":{"type":"number","description":"依赖freq，重复周期。如：frequency=DAILY;interval=8. 每隔8天重复一次"},"monthdays":{"type":"array","items":{"type":"number"},"description":"单月内，哪些天"},"months":{"type":"array","items":{"type":"number"},"description":"一年内，哪些月份"}},"required":["frequency","count","until","days","interval","monthdays","months"],"additionalProperties":false,"description":"日历重复规则，示例：\"recurrence\": {     \"frequency\": \"DAILY\",     \"interval\":2,     \"count\":10 } 表示：每隔一天，共发生10次。  改字段在创建普通日程，或者没有明确表示需要重复的日程时不填"}},"required":["calUid","content","summary","receiverUserList","reminder","startTime","endTime","location"],"additionalProperties":false}},{"name":"deleteCalendar","description":"删除日历服务","inputSchema":{"type":"object","properties":{"calUid":{"type":"string","description":"待更新日历的日历id"},"receiverUserList":{"type":"array","items":{"type":"string"},"description":"接受者ID集合，最大容量500, 需要删除的人员列表"}},"required":["calUid","receiverUserList"],"additionalProperties":false}},{"name":"queryFreebusy","description":"查询用户闲忙和日程安排服务，用于查询用户在指定时间内是否有会议和日程的冲突","inputSchema":{"type":"object","properties":{"users":{"type":"array","items":{"type":"string"},"description":"需要查询人员列表，最大100"},"startTime":{"type":"string","description":"日历查询开始时间，GMT+0800（时区）时间字符串，格式为: YYYY-MM-DD HH:MM 如果没有指定开始时间或填空串，则表示当天开始查询"},"endTime":{"type":"string","description":"日历查询结束时间，GMT+0800（时区）时间字符串，格式为: YYYY-MM-DD HH:MM 如果没有指定开始时间或填空串，则表示开始时间往后1天"},"calUid":{"type":"string","description":"日历id,用于根据uid排除查询某日程"}},"required":["users","startTime","endTime"],"additionalProperties":false}}]', 'sse', 'SSE', null, '1', null, '空白模板233-g6dypa9D-AIAE', null, '533c8eef0d55426d9f58ed4298f3a88c', 'test_project_id', 'default', null);
DELETE
FROM ws_mcp_service_def
WHERE id = 'c4cecf677112402abd6dca31e8babe2b';
INSERT INTO ws_mcp_service_def (id, name, name_en, description, description_en, fc_instance_url, fc_instance_id,
                                fc_instance_status, fc_region, apig_group_id, apig_instance_id, deleted, tenant_id,
                                dept_code, created_date, created_by_user_id, last_updated_date, last_updated_by_user_id,
                                readme, server_config, tools, deploy_type, org_type, function_name, server_id,
                                function_urn, function_application_name, domain_id, unique_code, project_id,
                                workspace_id)
VALUES ('c4cecf677112402abd6dca31e8babe2b', '空白模板0', 'Empty Template', '基于空白模板快速构建自己的业务逻辑0',
        'Base on empty template to create mcp service', '', null, 'success', null, null, null, 0, 'system', 'system',
        '2025-08-08 11:08:50', 'system', '2025-08-08 15:13:01', '2', null, '{
  "mcpServers" : {
    "example-sse" : {
      "url" : "http://xxxx:8080/sse"
    }
  }
}', '[{"name":"queryTestCaseList","description":"查询一个Agent智能体提示词所有的测试用例列表，包含了每个测试用例的详细信息。注意：获取到数据后应该用表格呈现。","inputSchema":{"type":"object","properties":{"promptId":{"type":"string","description":"提示词的ID。注意：不要捏造ID，如果你不知道ID，应该询问用户修改哪一个提示词。"}},"required":["promptId"],"additionalProperties":false}},{"name":"deleteTestTaskById","description":"删除一个测试历史任务。","inputSchema":{"type":"object","properties":{"testTaskId":{"type":"string","description":"测试任务的ID。"}},"required":["testTaskId"],"additionalProperties":false}},{"name":"saveTestResult","description":"测试结果保存，用来保存测试任务的结果，你应该在完成所有的用户要求的测试任务后立即调用此工具保存测试结果。","inputSchema":{"type":"object","properties":{"testTaskResult":{"type":"object","properties":{"prompt_content":{"type":"string","description":"Agent智能体的提示词内容"},"prompt_id":{"type":"string","description":"提示词的ID。注意：不要捏造ID，如果你不知道ID，应该询问用户修改哪一个提示词。"},"prompt_type":{"type":"string","description":"提示词的类型。"},"prompt_version":{"type":"integer","format":"int32","description":"提示词版本号。"},"test_case_result_list":{"description":"本次的所有测试用例的测试结果。","type":"array","items":{"type":"object","properties":{"score":{"type":"integer","format":"int32","description":"根据测试的真实结果testCaseOutputActualValue和期望值testCaseOutputExpectedValue，对本次测试进行打分。打分规则：1、如果提示词的类型是function，testCaseOutputActualValue和testCaseOutputExpectedValue完全相等，则是满分10分；否则是0分。2、如果提示词的类型是common，请结合原始提示词、测试用例的原始信息对本次测试结果进行综合评价打分，如果输出包含了所有期望的要点则满足10分，包含大部分期望要点得6到9分，包含一半期望要点得5分，包含期望要点较少得1~4分，输出很差不包含任何期望的要点得0分。"},"test_case_id":{"type":"string","description":"测试用例的ID。"},"test_case_input":{"type":"string","description":"测试用例的输入。"},"test_case_name":{"type":"string","description":"测试用例名称。注意：相同提示词的测试用例名称不能重名。"},"test_case_output_actual_value":{"type":"string","description":"测试的真实输出。"},"test_case_output_expected_value":{"type":"string","description":"测试的期望输出。"},"test_review":{"type":"string","description":"根据测试的真实结果和期望值，对本次测试进行评价。"}},"required":["score","test_case_id","test_case_input","test_case_name","test_case_output_actual_value","test_case_output_expected_value","test_review"],"description":"本次的所有测试用例的测试结果。"}}},"required":["prompt_content","prompt_id","prompt_type","prompt_version","test_case_result_list"],"description":"执行测试后返回的测试结果集合。"}},"required":["testTaskResult"],"additionalProperties":false}},{"name":"modifyPromptById","description":"修改一个Agent智能体提示词的内容。","inputSchema":{"type":"object","properties":{"promptId":{"type":"string","description":"提示词的ID。注意：不要捏造ID，如果你不知道ID，应该询问用户修改哪一个提示词。"},"promptContent":{"type":"string","description":"Agent智能体的提示词内容。注意：提示词应该是结构化的，使用markdown格式，要有清晰准确的角色、任务、要求、示例等说明，确保Agent出色地按照要求完成任务。"}},"required":["promptId","promptContent"],"additionalProperties":false}},{"name":"testPrompt","description":"执行一次测试Agent智能体提示词的测试用例，一次支持执行多个测试用例，返回测试的输出结果。注意：1、你应该先获取到被测试提示词的原始信息，以及测试用例的原始信息，然后再使用此工具进行测试获取测试结果。2、此工具只是执行测试获取测试结果，当完成所有的用户要求的测试任务后你应该立即调用测试结果保存工具，把所有的测试结果进行保存，否则用户将无法看到测试结果。3、最终结果可以用表格呈现。","inputSchema":{"type":"object","properties":{"testTaskInfo":{"type":"object","properties":{"prompt_content":{"type":"string","description":"待测试的Agent智能体提示词的原始内容，你需要先从工具获取提示词内容，不要捏造提示词内容；当使用此工具的时候，你应该把这个提示词的内容临时作为你的系统提示词。"},"prompt_id":{"type":"string","description":"提示词的ID。注意：不要捏造ID，如果你不知道ID，应该询问用户修改哪一个提示词。"},"prompt_type":{"type":"string","description":"提示词的类型。"},"prompt_version":{"type":"integer","format":"int32","description":"提示词版本号。"},"test_case_info_list":{"description":"被测试的测试用例的信息集合。","type":"array","items":{"type":"object","properties":{"test_caseId":{"type":"string","description":"测试用例的ID。"},"test_case_input":{"type":"string","description":"测试用例的原始输入，你需要先从工具获取测试用例的输入，不要捏造输入；当使用此工具的时候，你应该把测试用例的输入临时作为用户的输入。"},"test_case_name":{"type":"string","description":"测试用例名称。注意：相同提示词的测试用例名称不能重名。"},"test_case_predicted_output":{"type":"string","description":"你对前测试用例的预测输出，不能为空，需要动态生成；当使用此工具的时候，你应该根据临时的系统提示词promptContent和临时的用户的输入testCaseInput，分析并生成对应的预测输出，并设置到此参数上。"}},"required":["test_caseId","test_case_input","test_case_name","test_case_predicted_output"],"description":"被测试的测试用例的信息集合。"}}},"required":["prompt_content","prompt_id","prompt_type","prompt_version","test_case_info_list"],"description":"测试任务的提示词和测试用例信息。"}},"required":["testTaskInfo"],"additionalProperties":false}},{"name":"queryPromptById","description":"查询一个Agent智能体提示词的详细信息。","inputSchema":{"type":"object","properties":{"promptId":{"type":"string","description":"提示词的ID。注意：不要捏造ID，如果你不知道ID，应该询问用户修改哪一个提示词。"}},"required":["promptId"],"additionalProperties":false}},{"name":"queryTestTaskList","description":"查询一个Agent智能体提示词所有的测试历史任务列表，包含了每个测试任务的详细信息。注意：获取到数据后应该用表格呈现。","inputSchema":{"type":"object","properties":{"promptId":{"type":"string","description":"提示词的ID。注意：不要捏造ID，如果你不知道ID，应该询问用户修改哪一个提示词。"}},"required":["promptId"],"additionalProperties":false}},{"name":"createPrompt","description":"为一个Agent智能体创建一个提示词。","inputSchema":{"type":"object","properties":{"promptName":{"type":"string","description":"提示词名称。注意：提示词的名称不能重名。"},"promptContent":{"type":"string","description":"Agent智能体的提示词内容。注意：提示词应该是结构化的，使用markdown格式，要有清晰准确的角色、任务、要求、示例等说明，确保Agent出色地按照要求完成任务。"},"type":{"type":"string","description":"提示词的类型，取值范围是：function、common。解释说明：function表示函数模式，需要Agent根据提示词输出固定范围的值（比如输出：Y或N、存在或不存在、合法或不合法等有限的固定序列值）；common表示通用模式，输出内容是不固定的。"}},"required":["promptName","promptContent","type"],"additionalProperties":false}},{"name":"queryPromptList","description":"查询所有的Agent智能体提示词列表。注意：获取到数据后应该用表格呈现。","inputSchema":{"type":"object","properties":{},"required":[],"additionalProperties":false}},{"name":"deleteTestCaseById","description":"删除一个测试用例。","inputSchema":{"type":"object","properties":{"testCaseId":{"type":"string","description":"测试用例的ID。"}},"required":["testCaseId"],"additionalProperties":false}},{"name":"modifyTestCase","description":"修改一个测试用例的输入和期望输出。","inputSchema":{"type":"object","properties":{"testCaseId":{"type":"string","description":"测试用例的ID。"},"testCaseInput":{"type":"string","description":"测试用例的输入。"},"testCaseOutput":{"type":"string","description":"测试用例的期望输出。"}},"required":["testCaseId","testCaseInput","testCaseOutput"],"additionalProperties":false}},{"name":"queryTestCaseById","description":"查询一个测试用例的详细信息。","inputSchema":{"type":"object","properties":{"testCaseId":{"type":"string","description":"提示词的ID。注意：不要捏造ID，如果你不知道ID，应该询问用户修改哪一个提示词。"}},"required":["testCaseId"],"additionalProperties":false}},{"name":"queryTestTaskById","description":"查询一个测试历史任务的详细信息。","inputSchema":{"type":"object","properties":{"testTaskId":{"type":"string","description":"测试任务的ID。"}},"required":["testTaskId"],"additionalProperties":false}},{"name":"deletePromptById","description":"删除一个Agent智能体提示词。","inputSchema":{"type":"object","properties":{"promptId":{"type":"string","description":"提示词的ID。注意：不要捏造ID，如果你不知道ID，应该询问用户修改哪一个提示词。"}},"required":["promptId"],"additionalProperties":false}},{"name":"createTestCaseForPrompt","description":"给一个Agent智能体提示词创建一个测试用例。","inputSchema":{"type":"object","properties":{"promptId":{"type":"string","description":"提示词的ID。注意：不要捏造ID，如果你不知道ID，应该询问用户修改哪一个提示词。"},"testCaseName":{"type":"string","description":"测试用例名称。注意：相同提示词的测试用例名称不能重名。"},"testCaseInput":{"type":"string","description":"测试用例的输入。"},"testCaseOutput":{"type":"string","description":"测试用例的期望输出。"}},"required":["promptId","testCaseName","testCaseInput","testCaseOutput"],"additionalProperties":false}}]', 'sse', 'SSE', null, '1', null, '空白模板0-F8npGq3l-AIAE', null, 'c2b1a3775c6b4fe195ddb3d0c067b4c1', 'test_project_id', 'default');
DELETE
FROM ws_mcp_service_def
WHERE id = 'ff824f837e954cd5ad9036f45a9e8b34';
INSERT INTO ws_mcp_service_def (id, name, name_en, description, description_en, fc_instance_url, fc_instance_id,
                                fc_instance_status, fc_region, apig_group_id, apig_instance_id, deleted, tenant_id,
                                dept_code, created_date, created_by_user_id, last_updated_date, last_updated_by_user_id,
                                readme, server_config, tools, deploy_type, org_type, function_name, server_id,
                                function_urn, function_application_name, domain_id, unique_code, project_id,
                                workspace_id)
VALUES ('ff824f837e954cd5ad9036f45a9e8b34', '空白模板java', 'Empty Template', '基于空白模板快速构建自己的业务逻辑',
        'Base on empty template to create mcp service', '', null, 'fail', null, null, null, 0, 'system', 'system',
        '2025-08-07 17:41:01', 'system', '2025-08-07 17:41:58', '2', null, '{
  "mcpServers" : {
    "example-sse" : {
      "url" : "http://xxxx:8080/sse"
    }
  }
}', '', 'sse', 'SSE', null, '1', null, '空白模板java-z9uF5aUD-AIAE', null, '46dbccdc841047659a9b39f56dae87fd', 'test_project_id', 'default');
DELETE
FROM ws_mcp_service_def
WHERE id = 'ff824f837e954cd5ad9036f45a9e8b35';
INSERT INTO ws_mcp_service_def (id, name, name_en, description, description_en, fc_instance_url, fc_instance_id,
                                fc_instance_status, fc_region, apig_group_id, apig_instance_id, deleted, tenant_id,
                                dept_code, created_date, created_by_user_id, last_updated_date, last_updated_by_user_id,
                                readme, server_config, tools, deploy_type, org_type, function_name, server_id,
                                function_urn, function_application_name, domain_id, unique_code, project_id,
                                workspace_id)
VALUES ('ff824f837e954cd5ad9036f45a9e8b35', '空白模板java', 'Empty Template', '基于空白模板快速构建自己的业务逻辑',
        'Base on empty template to create mcp service',
        'AAAAAgAAAAAAAAAAAAAAAQAAAAdpkliySfoJ88ElgSFsP7imTQ8YkmwK5LS9p8Okw+RmnAEAAAEAAAAAAAAAICkAmYNv9QD/1WAtf6iTBvTMv6FDlZ8r3X7ea5ohm7Nx',
        null, 'fail', null, null, null, 0, 'system', 'system', '2025-08-07 17:41:01', 'system', '2025-08-07 17:41:58',
        '2', null, '{
  "mcpServers" : {
    "example-sse" : {
      "url" : "http://xxxx:8080/sse"
    }
  }
}', '', 'sse', 'NPX', null, '1', null, '空白模板java-z9uF5aUD-AIAE', null, '46dbccdc841047659a9b39f56dae87fd', 'test_project_id', 'default');
DELETE
FROM ws_mcp_service_def
WHERE id = 'ff824f837e954cd5ad9036f45a9e8b36';
INSERT INTO ws_mcp_service_def (id, name, name_en, description, description_en, fc_instance_url, fc_instance_id,
                                fc_instance_status, fc_region, apig_group_id, apig_instance_id, deleted, tenant_id,
                                dept_code, created_date, created_by_user_id, last_updated_date, last_updated_by_user_id,
                                readme, server_config, tools, deploy_type, org_type, function_name, server_id,
                                function_urn, function_application_name, domain_id, unique_code, project_id,
                                workspace_id)
VALUES ('ff824f837e954cd5ad9036f45a9e8b36', '空白模板java', 'Empty Template', '基于空白模板快速构建自己的业务逻辑',
        'Base on empty template to create mcp service',
        'AAAAAgAAAAAAAAAAAAAAAQAAAAdpkliySfoJ88ElgSFsP7imTQ8YkmwK5LS9p8Okw+RmnAEAAAEAAAAAAAAAICkAmYNv9QD/1WAtf6iTBvTMv6FDlZ8r3X7ea5ohm7Nx',
        null, 'fail', null, null, null, 0, 'system', 'system', '2025-08-07 17:41:01', 'system', '2025-08-07 17:41:58',
        '2', null, '{
  "mcpServers" : {
    "example-sse" : {
      "url" : "http://xxxx:8080/sse"
    }
  }
}', '', 'sse', 'NPX', null, '914', null, '空白模板java-z9uF5aUD-AIAE', null, '46dbccdc841047659a9b39f56dae87fd', 'test_project_id', 'default');
DELETE
FROM ws_mcp_service_def
WHERE id = 'ff824f837e954cd5ad9036f45a9e8b37';
INSERT INTO ws_mcp_service_def (id, name, name_en, description, description_en, fc_instance_url, fc_instance_id,
                                fc_instance_status, fc_region, apig_group_id, apig_instance_id, deleted, tenant_id,
                                dept_code, created_date, created_by_user_id, last_updated_date, last_updated_by_user_id,
                                readme, server_config, tools, deploy_type, org_type, function_name, server_id,
                                function_urn, function_application_name, domain_id, unique_code, project_id,
                                workspace_id)
VALUES ('ff824f837e954cd5ad9036f45a9e8b37', '空白模板java', 'Empty Template', '基于空白模板快速构建自己的业务逻辑',
        'Base on empty template to create mcp service',
        'AAAAAgAAAAAAAAAAAAAAAQAAAAdpkliySfoJ88ElgSFsP7imTQ8YkmwK5LS9p8Okw+RmnAEAAAEAAAAAAAAAICkAmYNv9QD/1WAtf6iTBvTMv6FDlZ8r3X7ea5ohm7Nx',
        null, 'fail', null, null, null, 0, 'system', 'system', '2025-08-07 17:41:01', 'system', '2025-08-07 17:41:58',
        '2', null, '{
  "mcpServers" : {
    "example-sse" : {
      "url" : "http://xxxx:8080/sse"
    }
  }
}', '', 'sse', 'SSE', null, '1', null, '空白模板java-z9uF5aUD-AIAE', null, '46dbccdc841047659a9b39f56dae87fd', 'test_project_id', 'default');
DELETE
FROM ws_mcp_service_def
WHERE id = 'ff824f837e954cd5ad9036f45a9e8b38';
INSERT INTO ws_mcp_service_def (id, name, name_en, description, description_en, fc_instance_url, fc_instance_id,
                                fc_instance_status, fc_region, apig_group_id, apig_instance_id, deleted, tenant_id,
                                dept_code, created_date, created_by_user_id, last_updated_date, last_updated_by_user_id,
                                readme, server_config, tools, deploy_type, org_type, function_name, server_id,
                                function_urn, function_application_name, domain_id, unique_code, project_id,
                                workspace_id)
VALUES ('ff824f837e954cd5ad9036f45a9e8b38', '空白模板java', 'Empty Template', '基于空白模板快速构建自己的业务逻辑',
        'Base on empty template to create mcp service',
        'AAAAAgAAAAAAAAAAAAAAAQAAAAdpkliySfoJ88ElgSFsP7imTQ8YkmwK5LS9p8Okw+RmnAEAAAEAAAAAAAAAICkAmYNv9QD/1WAtf6iTBvTMv6FDlZ8r3X7ea5ohm7Nx',
        null, 'fail', null, null, null, 0, 'system', 'system', '2025-08-07 17:41:01', 'system', '2025-08-07 17:41:58',
        '2', null, '{
  "mcpServers" : {
    "example-sse" : {
      "url" : "http://xxxx:8080/sse"
    }
  }
}', '', 'sse', 'SSE', null, '1', null, '空白模板java-z9uF5aUD-AIAE', null, '46dbccdc841047659a9b39f56dae87fd', 'test_project_id', 'default');
DELETE
FROM ws_mcp_service_def
WHERE id = 'ff824f837e954cd5ad9036f45a9e8b39';
INSERT INTO ws_mcp_service_def (id, name, name_en, description, description_en, fc_instance_url, fc_instance_id,
                                fc_instance_status, fc_region, apig_group_id, apig_instance_id, deleted, tenant_id,
                                dept_code, created_date, created_by_user_id, last_updated_date, last_updated_by_user_id,
                                readme, server_config, tools, deploy_type, org_type, function_name, server_id,
                                function_urn, function_application_name, domain_id, unique_code, project_id,
                                workspace_id)
VALUES ('ff824f837e954cd5ad9036f45a9e8b39', '空白模板java', 'Empty Template', '基于空白模板快速构建自己的业务逻辑',
        'Base on empty template to create mcp service',
        'AAAAAgAAAAAAAAAAAAAAAQAAAAdpkliySfoJ88ElgSFsP7imTQ8YkmwK5LS9p8Okw+RmnAEAAAEAAAAAAAAAICkAmYNv9QD/1WAtf6iTBvTMv6FDlZ8r3X7ea5ohm7Nx',
        null, 'fail', null, null, null, 0, 'system', 'system', '2025-08-07 17:41:01', 'system', '2025-08-07 17:41:58',
        '2', null, '{
  "mcpServers" : {
    "example-sse" : {
      "url" : "http://xxxx:8080/sse"
    }
  }
}', '', 'sse', 'SSE', null, '1', null, '空白模板java-z9uF5aUD-AIAE', null, '46dbccdc841047659a9b39f56dae87fd', 'test_project_id', 'default');
DELETE
FROM ws_mcp_service_def
WHERE id = 'ff824f837e954cd5ad9036f45a9e8b40';
INSERT INTO ws_mcp_service_def (id, name, name_en, description, description_en, fc_instance_url, fc_instance_id,
                                fc_instance_status, fc_region, apig_group_id, apig_instance_id, deleted, tenant_id,
                                dept_code, created_date, created_by_user_id, last_updated_date, last_updated_by_user_id,
                                readme, server_config, tools, deploy_type, org_type, function_name, server_id,
                                function_urn, function_application_name, domain_id, unique_code, project_id,
                                workspace_id)
VALUES ('ff824f837e954cd5ad9036f45a9e8b40', '空白模板java', 'Empty Template', '基于空白模板快速构建自己的业务逻辑',
        'Base on empty template to create mcp service',
        'AAAAAgAAAAAAAAAAAAAAAQAAAAdpkliySfoJ88ElgSFsP7imTQ8YkmwK5LS9p8Okw+RmnAEAAAEAAAAAAAAAICkAmYNv9QD/1WAtf6iTBvTMv6FDlZ8r3X7ea5ohm7Nx',
        null, 'fail', null, null, null, 0, 'system', 'system', '2025-08-07 17:41:01', 'system', '2025-08-07 17:41:58',
        '2', null, '{
  "mcpServers" : {
    "example-sse" : {
      "url" : "http://xxxx:8080/sse"
    }
  }
}', '', 'sse', 'SSE', null, '1', null, '空白模板java-z9uF5aUD-AIAE', null, '46dbccdc841047659a9b39f56dae87fd', 'test_project_id', 'default');


INSERT INTO `t_share_scope` (`id`, `resource_id`, `resource_type`, `workspace_id`, `project_id`, `tenant_id`) VALUES ('fbaaab68e32f49bb9b5390dc861d3ff4', '305d74a28fce4179934fceac17e32910', 'mcp', 'all', 'test_project_id', '2f43a709ad5f4a8f93d9c44c7272d053');
INSERT INTO `t_share_resource` (`resource_id`, `resource_name`, `workspace_id`, `workspace_name`, `trace_id`, `version_list`, `project_id`, `tenant_id`, `creator_id`, `creator`, `create_time`, `updater_id`, `updater`, `update_time`, `resource_type`) VALUES ('305d74a28fce4179934fceac17e32910', '空白模板NPXX', 'test_workspace_id', '个人空间', '305d74a28fce4179934fceac17e32910', '[]', 'test_project_id', '2f43a709ad5f4a8f93d9c44c7272d053', '0c2967dd5acb4e909d0ef7e98db9f0ee', 'jiangtao01', '2026-01-26 19:37:49', '0c2967dd5acb4e909d0ef7e98db9f0ee', 'jiangtao01', '2026-01-26 19:37:49', 'mcp');




