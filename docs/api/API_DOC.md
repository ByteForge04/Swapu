# SwapU 接口文档

## AdminController
**基础路径**: `/admin`

### userList
- **接口路径**: `/admin/user/list`
- **请求方式**: `GET`
- **返回值**: `Result<?>`
- **参数**:
  - `(defaultValue = "1"`

### updateUserStatus
- **接口路径**: `/admin/user/status/{userId}/{status}`
- **请求方式**: `POST`
- **返回值**: `Result<?>`
- **参数**:
  - `Long userId`
  - `Integer status`

### itemList
- **接口路径**: `/admin/item/list`
- **请求方式**: `GET`
- **返回值**: `Result<?>`
- **参数**:
  - `(defaultValue = "1"`

### updateItemStatus
- **接口路径**: `/admin/item/status/{itemId}/{status}`
- **请求方式**: `POST`
- **返回值**: `Result<?>`
- **参数**:
  - `Long itemId`
  - `Integer status`

### syncEs
- **接口路径**: `/admin/es/sync`
- **请求方式**: `POST`
- **返回值**: `Result<?>`
- **参数**: 无

### dashboard
- **接口路径**: `/admin/dashboard`
- **请求方式**: `GET`
- **返回值**: `Result<?>`
- **参数**: 无

## CommentController
**基础路径**: `/comment`

### create
- **接口路径**: `/comment/create`
- **请求方式**: `POST`
- **返回值**: `Result<?>`
- **参数**:
  - `Comment comment`

### getByOrder
- **接口路径**: `/comment/order/{orderId}`
- **请求方式**: `GET`
- **返回值**: `Result<Comment>`
- **参数**:
  - `Long orderId`

### getByItem
- **接口路径**: `/comment/item/{itemId}`
- **请求方式**: `GET`
- **返回值**: `Result<List<Comment>>`
- **参数**:
  - `Long itemId`

### getByUser
- **接口路径**: `/comment/user/{userId}`
- **请求方式**: `GET`
- **返回值**: `Result<List<Comment>>`
- **参数**:
  - `Long userId`

### getSellerComments
- **接口路径**: `/comment/public/seller/{userId}`
- **请求方式**: `GET`
- **返回值**: `Result<List<Comment>>`
- **参数**:
  - `Long userId`

## CommonController
**基础路径**: `/common`

### upload
- **接口路径**: `/common/upload`
- **请求方式**: `POST`
- **返回值**: `Result<String>`
- **参数**:
  - `MultipartFile file`

## ItemCategoryController
**基础路径**: `/category`

### list
- **接口路径**: `/category/list`
- **请求方式**: `GET`
- **返回值**: `Result<List<ItemCategory>>`
- **参数**: 无

### adminList
- **接口路径**: `/category/admin/list`
- **请求方式**: `GET`
- **返回值**: `Result<List<ItemCategory>>`
- **参数**: 无

### add
- **接口路径**: `/category/add`
- **请求方式**: `POST`
- **返回值**: `Result<?>`
- **参数**:
  - `ItemCategory category`

### update
- **接口路径**: `/category/update`
- **请求方式**: `PUT`
- **返回值**: `Result<?>`
- **参数**:
  - `ItemCategory category`

### delete
- **接口路径**: `/category/delete/{id}`
- **请求方式**: `DELETE`
- **返回值**: `Result<?>`
- **参数**:
  - `Integer id`

## ItemController
**基础路径**: `/item`

### publish
- **接口路径**: `/item/publish`
- **请求方式**: `POST`
- **返回值**: `Result<?>`
- **参数**:
  - `Item item`

### list
- **接口路径**: `/item/list`
- **请求方式**: `GET`
- **返回值**: `Result<List<Item>>`
- **参数**:
  - `Item item`

### search
- **接口路径**: `/item/search`
- **请求方式**: `GET`
- **返回值**: `Result<?>`
- **参数**:
  - `(required = false`

### suggest
- **接口路径**: `/item/suggest`
- **请求方式**: `GET`
- **返回值**: `Result<?>`
- **参数**:
  - `String keyword`

### detail
- **接口路径**: `/item/detail/{id}`
- **请求方式**: `GET`
- **返回值**: `Result<Item>`
- **参数**:
  - `Long id`

### myPublish
- **接口路径**: `/item/my/publish`
- **请求方式**: `GET`
- **返回值**: `Result<List<Item>>`
- **参数**: 无

### myWant
- **接口路径**: `/item/my/want`
- **请求方式**: `GET`
- **返回值**: `Result<List<Item>>`
- **参数**: 无

### updateStatus
- **接口路径**: `/item/status/{itemId}/{status}`
- **请求方式**: `POST`
- **返回值**: `Result<?>`
- **参数**:
  - `Long itemId`
  - `Integer status`

### delete
- **接口路径**: `/item/{itemId}`
- **请求方式**: `DELETE`
- **返回值**: `Result<?>`
- **参数**:
  - `Long itemId`

### listUserSelling
- **接口路径**: `/item/user/{userId}/selling`
- **请求方式**: `GET`
- **返回值**: `Result<List<Item>>`
- **参数**:
  - `Long userId`

### update
- **接口路径**: `/item/update`
- **请求方式**: `POST`
- **返回值**: `Result<?>`
- **参数**:
  - `Item item`

## ItemWantController
**基础路径**: `/item/want`

### toggle
- **接口路径**: `/item/want/{itemId}`
- **请求方式**: `POST`
- **返回值**: `Result<?>`
- **参数**:
  - `Long itemId`

### check
- **接口路径**: `/item/want/check/{itemId}`
- **请求方式**: `GET`
- **返回值**: `Result<Boolean>`
- **参数**:
  - `Long itemId`

## PaymentController
**基础路径**: `/pay`

### pay
- **接口路径**: `/pay/alipay`
- **请求方式**: `GET`
- **返回值**: `void`
- **参数**:
  - `Long orderId`

### returnCallback
- **接口路径**: `/pay/return`
- **请求方式**: `GET`
- **返回值**: `void`
- **参数**: 无

### notify
- **接口路径**: `/pay/notify`
- **请求方式**: `POST`
- **返回值**: `String`
- **参数**: 无

## ReportController
**基础路径**: `/report`

### create
- **接口路径**: `/report/create`
- **请求方式**: `POST`
- **返回值**: `Result<?>`
- **参数**:
  - `Report report`

### list
- **接口路径**: `/report/admin/list`
- **请求方式**: `GET`
- **返回值**: `Result<Page<Report>>`
- **参数**:
  - `(defaultValue = "1"`

### detail
- **接口路径**: `/report/admin/detail/{reportId}`
- **请求方式**: `GET`
- **返回值**: `Result<?>`
- **参数**:
  - `Long reportId`

### handle
- **接口路径**: `/report/admin/handle`
- **请求方式**: `POST`
- **返回值**: `Result<?>`
- **参数**:
  - `Report report`

## SysAnnouncementController
**基础路径**: `/announcement`

### list
- **接口路径**: `/announcement/list`
- **请求方式**: `GET`
- **返回值**: `Result<?>`
- **参数**:
  - `(defaultValue = "1"`

### adminList
- **接口路径**: `/announcement/admin/list`
- **请求方式**: `GET`
- **返回值**: `Result<?>`
- **参数**:
  - `(defaultValue = "1"`

### add
- **接口路径**: `/announcement/add`
- **请求方式**: `POST`
- **返回值**: `Result<?>`
- **参数**:
  - `SysAnnouncement announcement`

### update
- **接口路径**: `/announcement/update`
- **请求方式**: `PUT`
- **返回值**: `Result<?>`
- **参数**:
  - `SysAnnouncement announcement`

### delete
- **接口路径**: `/announcement/delete/{id}`
- **请求方式**: `DELETE`
- **返回值**: `Result<?>`
- **参数**:
  - `Long id`

### getById
- **接口路径**: `/announcement/{id}`
- **请求方式**: `GET`
- **返回值**: `Result<?>`
- **参数**:
  - `Long id`

## SysNotificationController
**基础路径**: `/notification`

### unreadCount
- **接口路径**: `/notification/unread-count`
- **请求方式**: `GET`
- **返回值**: `Result<?>`
- **参数**: 无

### read
- **接口路径**: `/notification/read/{id}`
- **请求方式**: `PUT`
- **返回值**: `Result<?>`
- **参数**:
  - `Long id`

### readAll
- **接口路径**: `/notification/read-all`
- **请求方式**: `PUT`
- **返回值**: `Result<?>`
- **参数**: 无

## TradeOrderController
**基础路径**: `/order`

### create
- **接口路径**: `/order/create`
- **请求方式**: `POST`
- **返回值**: `Result<?>`
- **参数**:
  - `TradeOrder order`

### confirm
- **接口路径**: `/order/confirm/{orderId}`
- **请求方式**: `POST`
- **返回值**: `Result<?>`
- **参数**:
  - `Long orderId`

### cancel
- **接口路径**: `/order/cancel/{orderId}`
- **请求方式**: `POST`
- **返回值**: `Result<?>`
- **参数**:
  - `Long orderId`

### complete
- **接口路径**: `/order/complete/{orderId}`
- **请求方式**: `POST`
- **返回值**: `Result<?>`
- **参数**:
  - `Long orderId`

### list
- **接口路径**: `/order/list/{type}`
- **请求方式**: `GET`
- **返回值**: `Result<List<TradeOrder>>`
- **参数**:
  - `Integer type`

### detail
- **接口路径**: `/order/detail/{orderId}`
- **请求方式**: `GET`
- **返回值**: `Result<TradeOrder>`
- **参数**:
  - `Long orderId`

## UserController
**基础路径**: `/user`

### login
- **接口路径**: `/user/login`
- **请求方式**: `POST`
- **返回值**: `Result<LoginVO>`
- **参数**:
  - `User user`

### register
- **接口路径**: `/user/register`
- **请求方式**: `POST`
- **返回值**: `Result<?>`
- **参数**:
  - `User user`

### getUserInfo
- **接口路径**: `/user/info`
- **请求方式**: `GET`
- **返回值**: `Result<User>`
- **参数**: 无

### update
- **接口路径**: `/user/info`
- **请求方式**: `PUT`
- **返回值**: `Result<?>`
- **参数**:
  - `User user`

### changePassword
- **接口路径**: `/user/password`
- **请求方式**: `POST`
- **返回值**: `Result<?>`
- **参数**:
  - `ChangePasswordDTO dto`

### updatePost
- **接口路径**: `/user/update`
- **请求方式**: `POST`
- **返回值**: `Result<?>`
- **参数**:
  - `User user`

### getUserDetail
- **接口路径**: `/user/admin/{userId}`
- **请求方式**: `GET`
- **返回值**: `Result<User>`
- **参数**:
  - `Long userId`

### getPublicUserInfo
- **接口路径**: `/user/public/{userId}`
- **请求方式**: `GET`
- **返回值**: `Result<User>`
- **参数**:
  - `Long userId`

