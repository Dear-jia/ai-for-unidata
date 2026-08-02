# 研数 UniData · 考研院校分数线数据平台

一个前后端分离的考研数据平台：用户注册登录后可开通会员查看各院校历年考研分数线；前台展示考研资讯与网站活动；管理端可管理用户、学校、分数线、资讯与活动数据。

## 数据规模（2026 年 8 月更新）

| 数据 | 内容 | 来源 |
| --- | --- | --- |
| 院校库 | **939 所研究生招生单位**（名称、省份、主管部门、层次、研招网院校信息页链接） | 研招网院校库 `yz.chsi.com.cn/sch` |
| 国家线 | **2025 / 2026 两年完整国家线**（A 类 / B 类总分与单科线，含学硕门类与专硕类别） | 研招网国家线公告、中公教育 |
| 34 所自划线院校 | **2025 / 2026 两年官方复试基本分数线**：提供官方原文链接跳转（研招网 / 各校官网），可解析的 HTML 表格同步入库为文本数据 | 研招网「34 所自划线院校复试线」专栏 |
| 院校详情兜底 | 每所院校详情页均有数据：无单独复试线收录时，展示**计算机相关学科（工学门类）国家线**（2025/2026）作为参考，并标注"以官方公布为准" | 教育部国家线 |

> 平台分数线数据**仅保留 2025、2026 两个年度**，更早年份已清理。

院校详情页可一键跳转对应招生单位的「招生简章 / 招生细则」（研招网院校信息页）；34 所自划线院校的官方复试线仅提供官方原文链接跳转，不复制、不展示第三方图表。

> 当前全站分数线数据（院校复试线、国家线、34 校官方复试线）**全部免费公开**，不区分登录状态；
> 会员注册 / 订单 / 有效期等基础能力已保留在代码中，会员专属特权待后续迭代上线。

## 数据来源与合规说明

- 平台仅收录**公开事实性数据**：院校名单与主管部门（研招网院校库公开信息）、教育部公布的全国硕士研究生国家线（政府公开信息）、可公开检索到的院校复试基本分数线文本；未收录单独数据的院校，页面展示计算机相关学科（工学门类）国家线作为参考。
- 34 所自划线院校的官方复试线**仅提供官方原文链接跳转**（研招网 / 各高校官网），不在本站存储或展示第三方图表图片，原始内容版权归其权利人所有。
- 数据在 `backend/src/main/resources/` 下以 CSV 形式随仓库维护，来源渠道均已在 README 与页面标注；正式上线后如需持续更新数据，建议通过官方发布渠道人工整理或与数据提供方沟通合作。
- 本站页面已包含免责声明、隐私政策、用户协议，正式上线前建议由法律专业人士审核。

## 上线前合规清单

- [ ] 服务器所在地备案：部署在中国大陆需完成 ICP 备案（工信部），并在页面底部展示备案号
- [ ] 法律文本审核：请律师审核免责声明 / 隐私政策 / 用户协议后再上线
- [ ] 数据来源标注：页面与文档中持续标注"数据以教育部及各高校官方发布为准"
- [ ] 安全配置：生产环境通过环境变量设置强 `JWT_SECRET`、数据库密码，关闭 H2 控制台，启用 HTTPS
- [ ] 数据库迁移：由开发环境 H2 切换到生产 MySQL（`application-mysql.yml` 与 `deploy/docker-compose.yml` 已就绪）
- [ ] 联系方式：在页面公开可用的联系 / 举报渠道（邮箱等）

## 功能总览

**用户端**
- 注册 / 登录（JWT 无状态鉴权，密码 BCrypt 加密）
- 个人中心：资料修改、密码修改
- 院校库：按名称 / 省份 / 类型 / 层次筛选，查看院校详情
- 分数线查询：按院校 / 年份 / 专业查询历年复试线、国家线（当前全部免费开放）
- 会员体系：月卡 / 季卡 / 年卡页面与订单流程已就绪，专属特权待后续开发（当前数据免费）
- 考研资讯：分类浏览、关键词搜索、阅读量统计
- 网站活动：活动列表展示

**管理端（`/admin`）**
- 数据总览：注册用户、VIP 会员、院校、分数线、资讯、订单统计
- 用户管理：搜索、编辑资料、启停用、重置密码、删除、角色与会员调整
- 学校管理：增删改查（删除学校会级联删除其分数线）
- 分数线管理：增删改查，可设置是否会员专享
- 资讯管理：发布 / 编辑 / 下架 / 删除，支持 HTML 正文
- 活动管理：增删改查

## 技术栈

| 端 | 技术 |
| --- | --- |
| 后端 | Java 17 · Spring Boot 3.2 · Spring Security · JWT (jjwt) · Spring Data JPA · H2(本地) / MySQL(生产) |
| 前端 | Vue 3 · Vite 5 · Element Plus · Pinia · Vue Router · Axios |
| 部署 | Docker Compose（Nginx + Spring Boot + MySQL 8） |

## 目录结构

```text
uniData-pro/
├── backend/                  # Spring Boot 后端
│   └── src/main/java/com/unidata/uni/
│       ├── config/           # 安全配置、全局异常、种子数据
│       ├── controller/       # REST 接口
│       ├── dto/              # 请求/响应对象
│       ├── entity/           # JPA 实体
│       ├── repository/       # 数据访问层
│       ├── security/         # JWT 过滤器/工具
│       └── service/          # 业务逻辑
├── frontend/                 # Vue 3 前端
│   └── src/
│       ├── api/              # axios 封装
│       ├── router/           # 路由与守卫
│       ├── stores/           # Pinia 状态
│       └── views/            # 页面（user 前台 / admin 后台）
└── deploy/                   # Docker Compose 部署
```

## 本地开发

环境要求：JDK 17+、Node.js 18+。

### 1. 启动后端

```bash
cd backend
./mvnw spring-boot:run        # 或 mvn spring-boot:run
```

- 默认使用 H2 文件数据库（`backend/data/`），无需安装数据库
- 首次启动自动创建表并写入演示数据
- 接口地址：http://localhost:8080
- H2 控制台：http://localhost:8080/h2-console（JDBC URL 见 `application.yml`）

### 2. 启动前端

```bash
cd frontend
npm install
npm run dev
```

浏览器访问 http://localhost:5173，开发服务器已将 `/api` 代理到后端 8080。

### 3. 演示账号

| 账号 | 密码 | 角色 |
| --- | --- | --- |
| `admin` | `admin123` | 管理员（管理后台 `/admin`） |
| `demo` | `demo123` | 已开通 1 年 VIP 的演示会员 |

## 核心接口

统一响应格式：`{ "code": 0, "message": "ok", "data": ... }`，`code != 0` 表示失败。

| 模块 | 方法 | 路径 | 说明 |
| --- | --- | --- | --- |
| 认证 | POST | `/api/auth/register` | 注册 |
| 认证 | POST | `/api/auth/login` | 登录，返回 token |
| 认证 | GET | `/api/auth/me` | 当前用户信息 |
| 公共 | GET | `/api/public/home` | 首页聚合数据 |
| 公共 | GET | `/api/public/schools` | 院校分页查询 |
| 公共 | GET | `/api/public/schools/{id}` | 院校详情 + 历年分数线 |
| 公共 | GET | `/api/public/scorelines` | 分数线查询（会员自动解锁专享数据） |
| 公共 | GET | `/api/public/articles` | 资讯列表 |
| 公共 | GET | `/api/public/articles/{id}` | 资讯详情（浏览量 +1） |
| 公共 | GET | `/api/public/activities` | 活动列表 |
| 会员 | POST | `/api/member/orders` | 创建会员订单 |
| 会员 | POST | `/api/member/orders/{orderNo}/activate` | 模拟支付并激活会员 |
| 会员 | GET | `/api/member/orders` | 我的订单 |
| 管理 | GET | `/api/admin/stats` | 平台统计 |
| 管理 | GET/PUT/DELETE | `/api/admin/users`、`/api/admin/schools`、`/api/admin/scorelines`、`/api/admin/articles`、`/api/admin/activities` | 数据管理 CRUD |

除 `/api/auth/**`、`/api/public/**` 外均需在请求头携带 `Authorization: Bearer <token>`；`/api/admin/**` 仅管理员可访问。

## 生产部署

### 方式一：Docker Compose（推荐）

需要 Docker 与 Docker Compose：

```bash
cd deploy
docker compose up -d --build
```

启动后：
- 前台：http://服务器IP
- 后端 API：http://服务器IP:8080
- 数据库：内置 MySQL 8 容器，数据保存在 `mysql-data` 卷中

环境变量（可用 `.env` 覆盖）：
- `MYSQL_ROOT_PASSWORD`：MySQL 密码（默认 `root123456`，**生产环境务必修改**）
- `JWT_SECRET`：JWT 签名密钥（**生产环境务必设置 32 字节以上随机值**）

### 方式二：手动部署（服务器已有 MySQL）

1. 创建数据库：

```sql
CREATE DATABASE unidata DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 构建并启动后端：

```bash
cd backend
./mvnw -DskipTests package
SPRING_PROFILES_ACTIVE=mysql \
DB_HOST=127.0.0.1 DB_PORT=3306 DB_NAME=unidata DB_USER=root DB_PASSWORD=你的密码 \
JWT_SECRET=你的随机密钥 \
java -jar target/uni-data-1.0.0.jar
```

3. 构建前端静态文件：

```bash
cd frontend
npm install
npm run build
```

4. 将 `frontend/dist` 部署到 Nginx，并把 `/api` 反向代理到后端：

```nginx
location /api/ {
    proxy_pass http://127.0.0.1:8080;
}
location / {
    root /var/www/unidata;
    try_files $uri $uri/ /index.html;
}
```

## 安全提示

- 首次部署必须修改 `JWT_SECRET`，否则 token 可被伪造
- 生产环境建议为 MySQL 设置独立账号而非 root
- 会员支付当前为演示流程（下单后直接模拟支付成功），接入真实支付时请在 `MemberService.activateOrder` 中校验支付回调
- 生产环境建议启用 HTTPS（Nginx + Let's Encrypt 等）

## 后续可扩展方向

- 接入真实支付（微信 / 支付宝）
- 分数线数据 Excel 批量导入
- 院校报录比、复试科目、导师信息等扩展字段
- 资讯评论、收藏
- 后台操作日志与权限细分
