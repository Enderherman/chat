create database easychat;

use easychat;

create table user_info
(
    user_id            varchar(12) primary key               not null comment '用户id',
    email              varchar(50) default null comment '邮箱',
    nick_name          varchar(40) default null comment '昵称',
    join_type          tinyint(1)  default null comment '添加好友方式: 0:直接添加,1:同意后添加',
    sex                tinyint(1)  default null comment '性别:0男1女',
    password           varchar(32) default null comment '密码',
    personal_signature varchar(64) default null comment '个性签名',
    status             tinyint(1)  default null comment '状态',
    create_time        datetime    default current_timestamp not null comment '创建时间',
    last_login_time    datetime    default null comment '最后登录时间',
    area_name          varchar(64) default null comment '地区',
    area_code          varchar(64) default null comment '地区编号',
    last_off_time      bigint(13)  default null comment '最后离开时间',
    is_delete          tinyint(1)  default 0                 not null comment '是否删除',
    unique key idx_key_email (email)
) comment '用户表';


create table user_info_beauty
(
    id      int(11) primary key not null comment '自增id',
    email   varchar(50)         not null comment '邮箱',
    user_id varchar(12)         not null comment '用户id',
    status  tinyint(1) comment '0:未使用 1:已使用',
    unique key idx_key_user_id (user_id),
    unique key idx_key_email (email)
) comment '靓号表';

create table group_info
(
    group_id     varchar(12) primary key not null comment '群组id',
    group_name   varchar(32)             null default null comment '群组昵称',
    group_own_id varchar(12)             null default null comment '群主用户id',
    create_time  datetime                     default current_timestamp comment '创建时间',
    group_notice varchar(500)            null default null comment '创建时间',
    join_type    tinyint(1)              null default null comment '加入方式: 0:直接加入 1:管理员同意后加入',
    status       tinyint(1)              null default 1 comment '状态： 0:解散 1:正常'
) comment '群组信息表';

create table user_contact
(
    user_id      varchar(12) not null comment '用户id',
    contact_id   varchar(12) not null comment '联系人id或者群组id',
    contact_type tinyint(1)  null default null comment '联系人类型: 0:好友 1:群组',
    create_time  datetime         default CURRENT_TIMESTAMP not null comment '创建时间',
    status       tinyint(1)  null default null comment '状态: 0:非好友 1:好友 2:已删除好友 3:被好友删除 4:已拉黑好友 5:被好友拉黑',
    update_time  datetime         default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '最后更新时间',
    primary key (user_id, contact_id) using btree,
    index idx_contact_id (contact_id) using btree
) comment '联系人表';

create table user_contact_apply
(
    apply_id        int(11) primary key not null auto_increment comment '自增id',
    apply_user_id   varchar(12)         not null comment '申请人id',
    receive_user_id varchar(12)         not null comment '接收人id',
    contact_type    tinyint(1)          not null comment '联系人类型 0:好友 1:群组',
    contact_id      varchar(12)         null     default null comment '联系人id或群组id 取决于加人还是加好友',
    last_apply_time bigint              null     default null comment '最后申请时间',
    status          tinyint(1)          not null default 0 comment '状态 0:待处理 1:已同意 2:已拒绝 3:已拉黑',
    apply_info      varchar(100)        null     default null comment '申请信息',
    unique index idx_key (apply_id, receive_user_id, contact_id),
    index idx_last_apply_time (last_apply_time)
)comment '联系人申请表';
