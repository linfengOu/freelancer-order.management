# 规格文档

## 架构图
    Front end
        |
     Servlets
        |
     Services
        |
      Daos
        |
	Database

## 用例
网站使用者分为两个角色：订单发布者，订单接受者

### 订单发布者
* 新建订单，制定 价格／分成比／要求完成时间／订单位置／保证金，即时现实收益比
* 发布订单，如果有人接受订单 收到通知，如果有人退出订单 收到通知
* 查看历史发布订单，订单显示对应状态
* 查询月／年订单发布量和金额
* 查询月／年收入和支出

### 订单接受者：
* 查看在榜订单
* 接受订单，会发送通知给发布者
* 接受的订单，选择退出，会发送通知给发布者
* 查看历史接单，订单显示对应状态
* 查询月／年完成量和收入

## Database design
* 用户表 user：<u>uid</u>, name(char(16)), pw(char(40)), utype(tinyint(1)), joindate(datetime)
* 订单表 order：<u>oid</u>, title(char(40)), price(float(8,1)), adiv(tinyint(1)), bdiv(tinyint(1)), deadline(datetime), place(char(40)), deposit(float(6,1))
* 订单描述表 orderdesc: <u>oid</u>, desc(varchar(255))
* 订单状态表 orderstatus: <u>oid</u>, <u>promulgator</u>, <u>applicant</u>, status(tinyint(1)), isread(tinyint(1))

