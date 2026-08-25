# AIO Volume 更新安全说明

## 适用范围

本说明只覆盖 AIO 容器的日常更新命令静态检查。远端 AIO 部署由外部团队管理；本仓库不拥有其 Compose 文件，也不连接、探测、重建或修改远端部署。

备份与灾难恢复不在本说明的范围内。这一边界与已接受的设计保持一致。

## 持久化约定

- `/workspace` 是 AIO 的持久化工作区根目录。
- 该目录由命名 Docker Volume 挂载。日常更新只重建 AIO 容器，命名 Volume 会被保留，因此 `/workspace` 中的持久化内容不会随容器重建而删除。
- 更新操作应由远端部署的维护者在其自身的 Compose 配置和变更流程中执行；本仓库不提供或维护那份 Compose 文件。

可接受的容器级更新形态例如先拉取镜像、再强制重建容器：

```text
docker compose pull && docker compose up -d --force-recreate
```

在实际执行前，可将命令或脚本交给本仓库的静态检查器：

```text
python scripts/deployment-safety/check_aio_volume_command.py --command "docker compose pull && docker compose up -d --force-recreate"
python scripts/deployment-safety/check_aio_volume_command.py --file path/to/update-script.sh
```

检查器只读取输入文本并返回检查结果，绝不会执行任何命令。

## 更新禁令

在 AIO 更新过程中，绝对不要运行下列会删除命名 Volume 或其内容的操作：

- `docker compose down -v`
- `docker-compose down -v`
- `down --volumes`（无论选项位于命令的何处）
- `docker volume rm`
- `docker volume prune`
- `docker system prune --volumes`

脚本也会识别多行输入以及等价的选项顺序，例如 `docker system --volumes prune`。发现上述模式时，检查器会以非零退出码和中文“危险命令”诊断拒绝输入。
