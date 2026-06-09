/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.saml;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

@SuppressWarnings("unchecked")
public class UserInfoBean implements Serializable {

    /**
     * 内网用户
     */
    public final static String INTRANET_USER = "INTRANET_USER";

    /**
     * 内网公司用户（创建用户时使用）
     */
    public final static String INTRANET_CORP_USER = "INTRANET_CORP_USER";

    /**
     * 内网合作方用户（创建用户时使用）
     */
    public final static String INTRANET_PARTNER_USER = "INTRANET_PARTNER_USER";

    /**
     * 外网用户
     */
    public final static String INTERNET_USER = "INTERNET_USER";

    /**
     * 外网公司用户（创建用户时使用）
     */
    public final static String INTERNET_CORP_USER = "INTERNET_CORP_USER";

    /**
     * 外网注册用户（创建用户时使用）
     */
    public final static String INTERNET_INTER_USER = "INTERNET_INTER_USER";

    /**
     * 中文属性
     */
    public final static String KEY_LANG_CN = "cn";

    /**
     * 英文属性
     */
    public final static String KEY_LANG_EN = "en";

    /**
     * 对象类
     */
    public final static String KEY_OBJECTCLASS = "objectclass";

    /**
     * 用户id
     */
    public final static String KEY_UID = "uid";

    /**
     * 用户姓名，Full name (English and Chinese)
     */
    public final static String KEY_CN = "cn";

    /**
     * 姓氏
     */
    public final static String KEY_SN = "sn";

    /**
     * First name (English and Chinese)
     */
    public final static String KEY_GIVENNAME = "givenname";

    /**
     * Country，统一的国家编码
     */
    public final static String KEY_C = "c";

    /**
     * StateOrProvinceName
     */
    public final static String KEY_ST = "st";

    /**
     * 城市
     */
    public final static String KEY_CITY = "city";

    /**
     * StreetAddress
     */
    public final static String KEY_STREET = "street";

    /**
     * 办公地点
     */
    public final static String KEY_LOCATIONNAME = "locationname";

    /**
     * HuaWei department number(Up to 10 numeric)
     */
    public final static String KEY_DEPARTMENTNUMBER = "departmentnumber";

    /**
     * 员工号。Employee id(4-6 numeric)
     */
    public final static String KEY_EMPLOYEENUMBER = "employeenumber";

    public final static String KEY_EMPLOYEETYPE = "employeetype";

    /**
     * OrganizationalUnitName，单位名称
     */
    public final static String KEY_OU = "ou";

    /**
     * 职业
     */
    public final static String KEY_BUSINESSCATEGORY = "businesscategory";

    /**
     * 职务
     */
    public final static String KEY_TITLE = "title";

    /**
     * 描述
     */
    public final static String KEY_DESCRIPTION = "description";

    /**
     * Email address
     */
    public final static String KEY_MAIL = "mail";

    /**
     * 通讯地址
     */
    public final static String KEY_POSTALADDRESS = "postaladdress";

    /**
     * 邮政编码
     */
    public final static String KEY_POSTALCODE = "postalcode";

    /**
     * 邮政信箱
     */
    public final static String KEY_POSTOFFICEBOX = "postofficebox";

    /**
     *
     */
    public final static String KEY_PHYSICALDELIVERYOFFICENAME = "physicaldeliveryofficename";

    /**
     * 家庭通讯地址
     */
    public final static String KEY_HOMEPOSTALADDRESS = "homepostaladdress";

    /**
     * 办公室电话 Office phone number
     */
    public final static String KEY_TELEPHONENUMBER = "telephonenumber";

    /**
     * 移动电话
     */
    public final static String KEY_MOBILE = "mobile";

    /**
     * 家庭电话
     */
    public final static String KEY_HOMEPHONE = "homephone";

    /**
     * 传呼机号码
     */
    public final static String KEY_PAGER = "pager";

    /**
     * 传真
     */
    public final static String KEY_FACSIMILETELEPHONENUMBER = "facsimiletelephonenumber";

    /**
     * 电报
     */
    public final static String KEY_TELEXNUMBER = "telexnumber";

    /**
     *
     */
    public final static String KEY_TELETEXTERMINALIDENTIFIER = "teletexterminalidentifier";

    /**
     * 消息通知方式
     */
    public final static String KEY_PREFERREDDELIVERYMETHOD = "preferreddeliverymethod";

    /**
     * 语言
     */
    public final static String KEY_PREFERREDLANGUAGE = "preferredlanguage";

    /**
     * 密码。以byte[]的格式保存。
     */
    public final static String KEY_USERPASSWORD = "userpassword";

    /**
     * 数字证书
     */
    public final static String KEY_USERCERTIFICATE = "usercertificate";

    /**
     * 驾驶证号
     */
    public final static String KEY_CARLICENSE = "carlicense";

    /**
     * 相片
     */
    public final static String KEY_PHOTO = "photo";

    /**
     * 预留
     */
    public final static String KEY_JPEGPHOTO = "jpegphoto";

    /**
     * 领导DN
     */
    public final static String KEY_MANAGER = "manager";

    /**
     * 秘书DN
     */
    public final static String KEY_SECRETARY = "secretary";

    /**
     * 预留
     */
    public final static String KEY_REGISTEREDADDRESS = "registeredaddress";

    /**
     * 是否有上岗证xkf3670
     */
    public final static String KEY_DESTINATIONINDICATOR = "destinationindicator";

    /**
     * 预留
     */
    public final static String KEY_SEEALSO = "seealso";

    /**
     * 老系统的uid
     */
    public final static String KEY_INITIALS = "initials";

    /**
     * 初始积分
     */
    public final static String KEY_ROOMNUMBER = "roomnumber";

    /**
     * 个人头衔，在此做性别标识用：Ms. ，Mr.
     */
    public final static String KEY_PERSONALTITLE = "personaltitle";

    /**
     * 注册日期（LDAP记录生成日期）
     */
    public final static String KEY_CREATETIMESTAMP = "createtimestamp";

    /**
     * 上次密码修改日期
     */
    public final static String KEY_LASTPASSWORDCHECK = "lastpasswordcheck";

    /**
     * 账号生效日期
     */
    public final static String KEY_VALIDFROM = "validfrom";

    /**
     * 账号失效日期
     */
    public final static String KEY_VALIDTO = "validto";

    /**
     * 账号是否禁用 TRUE/FALSE
     */
    public final static String KEY_ISACCOUNTENABLED = "isaccountenabled";

    /**
     * 出生日期
     */
    public final static String KEY_BIRTHDAY = "birthday";

    /**
     * 证件类别（身份证，驾驶证...）
     */
    public final static String KEY_CREDENTIALTYPE = "credentialtype";

    /**
     * 证件号（身份证号码，驾驶证号码...）
     */
    public final static String KEY_CREDENTIALNUMBER = "credentialnumber";

    /**
     * 学历
     */
    public final static String KEY_EDUCATIONDEGREE = "educationdegree";

    /**
     * 绑定的IP
     */
    public final static String KEY_BINDIP = "bindip";

    /**
     * 服务号。
     */
    public final static String KEY_SERVICENUMBER = "servicenumber";

    /**
     * 预留
     */
    public final static String KEY_AUDIO = "audio";

    /**
     * 预留
     */
    public final static String KEY_LABELEDURI = "labeleduri";

    /**
     * 预留
     */
    public final static String KEY_X121ADDRESS = "x121address";

    /**
     * 预留
     */
    public final static String KEY_INTERNATIONALISDNNUMBER = "internationalisdnnumber";

    /**
     * 预留
     */
    public final static String KEY_USERSMIMECERTIFICATE = "usersmimecertificate";

    /**
     * 预留
     */
    public final static String KEY_USERPKCS12 = "userpkcs12";

    /**
     * 所属组
     */
    public final static String KEY_STATICMEMBEROF = "staticmemberof";

    /**
     * 能管理的组
     */
    public final static String KEY_MANAGEDOBJECTS = "managedobjects";

    /**
     * 用户最后一次登录时间
     */
    public final static String KEY_NTUSERLASTLOGON = "ntuserlastlogon";

    /**
     * 用户最后一次推出登录时间
     */
    public final static String KEY_NTUSERLASTLOGOFF = "ntuserlastlogoff";

    /**
     * 用户登录次数
     */
    public final static String KEY_NTUSERLOGONCOUNT = "ntuserlogoncount";

    /**
     * 用户显示名称。保存格式如下： cn=刘妍 en=mariana
     */
    public final static String KEY_DISPLAYNAME = "displayname";

    /**
     * 部门名称。中英文部门名称使用语言前缀作为区分 cn=中文部门名称 en=英文部门名称
     */
    public final static String KEY_DEPARTMENTNAME = "departmentname";

    /**
     * 用户所属组属性
     */
    public final static String KEY_IBM_ALLGROUPS = "ibm-allgroups";

    /**
     * 最后修改日期
     */
    public final static String KEY_MODIFYTIMESTAMP = "modifytimestamp";

    /**
     * 条目修改用户
     */
    public final static String KEY_MODIFIERSNAME = "modifiersname";

    /**
     * 创建修改用户
     */
    public final static String KEY_CREATORSNAME = "creatorsname";

    /**
     * 预留
     */
    public final static String KEY_ENTRYOWNER = "entryowner";

    /**
     * 预留
     */
    public final static String KEY_IBM_ENTRYUUID = "ibm-entryuuid";

    /**
     * 预留
     */
    public final static String KEY_OWNERPROPAGATE = "ownerpropagate";

    /**
     * 预留
     */
    public final static String KEY_OWNERSOURCE = "ownersource";

    /**
     * 预留
     */
    public final static String KEY_PWDACCOUNTLOCKEDTIME = "pwdaccountlockedtime";

    /**
     * 预留
     */
    public final static String KEY_PWDCHANGEDTIME = "pwdchangedtime";

    /**
     * 预留
     */
    public final static String KEY_PWDEXPIRATIONWARNED = "pwdexpirationwarned";

    /**
     * 预留
     */
    public final static String KEY_PWDGRACEUSETIME = "pwdgraceuseTime";

    /**
     * 预留
     */
    public final static String KEY_PWDHISTORY = "pwdhistory";

    /**
     * 预留
     */
    public final static String KEY_PWDRESET = "pwdreset";

    /**
     * 预留
     */
    public final static String KEY_SUBSCHEMASUBENTRY = "subschemasubentry";

    /**
     * 用户密级列表
     */
    public static final String KEY_USER_PROP_SECLEV = "chked_security_levels";

    /**
     * 全局uid
     */
    public static final String KEY_GUID = "guid";

    /**
     * UUID
     */
    public static final String KEY_UUID = "AttributeStr6";

    public static final String KEY_UUID_PREFIX = "uuid~";

    /**
     *
     */
    private static final long serialVersionUID = -5986447815199326409L;

    private String nameId;

    /**
     * LDAP中用户条目唯一标识。
     */
    private String dn;

    /**
     * 常用属性列表
     */
    private String uid;

    private String cn;

    private String sn;

    private String givenName;

    private String departmentNumber;

    private String employeeType;

    private String employeeNumber;

    private ArrayList<String> email;

    private ArrayList<String> displayName;

    private ArrayList<String> departmentName;

    private String[] groups;

    private String createTimeStamp;

    private String modifyTimeStamp;

    private String preferredlanguage;

    private String userType;

    /**
     * 帐号到期提醒的天数，为0表示不需要提醒
     */
    private int acctAlertDays;

    /**
     * 密码到期提醒的天数，为0表示不需要提醒
     */
    private int pwdAlertDays;

    private HashMap<String, Object> properties;

    public UserInfoBean(String userType) {
        this.userType = userType;
    }

    public UserInfoBean() {
    }

    /**
     * 将ArrayList转化为String数组
     *
     * @return String[]
     */
    private static String[] listToStringArray(Object obj) {
        if (obj == null) {
            return (null);
        }

        if (obj instanceof ArrayList) {
            ArrayList<String> array = (ArrayList<String>) obj;
            String[] result = new String[array.size()];
            array.toArray(result);
            return (result);
        } else if (obj instanceof String) {
            return (new String[] {(String) obj});
        }
        return (null);
    }

    /**
     * 将Object转化为ArrayList数组
     *
     * @param obj
     * @return ArrayList
     */
    private static ArrayList<String> listToArrayList(Object obj) {
        if (obj == null) {
            return null;
        }

        if (obj instanceof ArrayList) {
            ArrayList<String> array = (ArrayList<String>) obj;
            return array;
        } else if (obj instanceof String) {
            ArrayList<String> array = new ArrayList<>();
            array.add((String) obj);
            return array;
        }
        return null;
    }

    /**
     * 获取用户姓名
     *
     * @return String
     */
    public String getCn() {
        return cn;
    }

    /**
     * 设置用户名称
     *
     * @param cn
     */
    public void setCn(String cn) {
        this.cn = cn;
    }

    /**
     * 根据语言类型，返回部门名称，
     * langType的取值为，中文：UserInfoBean.KEY_LANG_CN；英文：UserInfoBean.KEY_LANG_EN。
     *
     * @param langType
     * @return String
     */
    public String getDepartmentName(String langType) {
        return (getLangPropertyFromArray(departmentName, langType));
    }

    /**
     * 返回部门名称
     *
     * @return ArrayList
     */
    public ArrayList<String> getDepartmentName() {
        return (departmentName);
    }

    /**
     * 设置用户部门名称
     *
     * @param departmentName
     */
    public void setDepartmentName(ArrayList<String> departmentName) {
        this.departmentName = departmentName;
    }

    /**
     * 获取用户部门代码
     *
     * @return String
     */
    public String getDepartmentNumber() {
        return departmentNumber;
    }

    /**
     * 设置用户部门代码
     *
     * @param departmentNumber
     */
    public void setDepartmentNumber(String departmentNumber) {
        this.departmentNumber = departmentNumber;
    }

    /**
     * 根据语言类型返回显示名称，
     * langType的取值为，中文：UserInfoBean.KEY_LANG_CN；英文：UserInfoBean.KEY_LANG_EN。
     *
     * @param langType
     * @return String
     */
    public String getDisplayName(String langType) {
        return (getLangPropertyFromArray(displayName, langType));
    }

    /**
     * 返回显示名称
     *
     * @return ArrayList
     */
    public ArrayList<String> getDisplayName() {
        return (displayName);
    }

    /**
     * 设置用户显示名称
     *
     * @param displayName
     */
    public void setDisplayName(ArrayList<String> displayName) {
        this.displayName = displayName;
    }

    /**
     * 获取用户DN
     *
     * @return String
     */
    public String getDn() {
        return dn;
    }

    /**
     * @param string
     */
    public void setDn(String string) {
        dn = string;
    }

    /**
     * 获取用户EMAIL列表
     *
     * @return String[]
     */
    public String[] getEmail() {
        return listToStringArray(email);
    }

    /**
     * 设置用户EMAIL信息，多Email
     *
     * @param email
     */
    public void setEmail(ArrayList<String> email) {
        this.email = email;
    }

    /**
     * 设置用户EMAIL信息，唯一Email
     *
     * @param email
     */
    public void setEmail(String email) {
        this.email = new ArrayList<>(1);
        this.email.add(email);
    }

    /**
     * 获取用户EMAIL列表
     *
     * @return ArrayList
     */
    public ArrayList<String> getEmailList() {
        return (email);
    }

    /**
     * 获取用户首选EMAIL
     *
     * @return String
     */
    public String getFirstEmail() {
        String[] mailList = listToStringArray(email);
        if ((mailList != null) && (mailList.length > 0)) {
            return (mailList[0]);
        }
        return (null);
    }

    /**
     * 获取工号
     *
     * @return String
     */
    public String getEmployeeNumber() {
        return employeeNumber;
    }

    /**
     * 设置工号
     *
     * @param employeeNumber
     */
    public void setEmployeeNumber(String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    /**
     * 获取用户员工类型
     *
     * @return String
     */
    public String getEmployeeType() {
        return employeeType;
    }

    /**
     * 设置员工类型
     *
     * @param employeeType
     */
    public void setEmployeeType(String employeeType) {
        this.employeeType = employeeType;
    }

    /**
     * 获取用户名称全拼
     *
     * @return String
     */
    public String getGivenName() {
        return givenName;
    }

    /**
     * 设置用户拼音
     *
     * @param givenName
     */
    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    /**
     * 根据键值获取用户属性，属性值如果是单值返回类型为String，属性值如果是多值返回类型为ArrayList，<br>
     * 使用该方法获取到Object后需要自己去判断返回值的类型，不能盲目的强制转换为String。
     *
     * @return Object
     * @throws Exception
     */
    public Object getProperty(String atributeType) {
        if (properties != null) {
            Object obj = properties.get(atributeType);
            return (obj);
        }
        return (null);
    }

    /**
     * 根据键值获取用户属性，返回String数组。
     *
     * @return String[]
     */
    public String[] getStringArrayProperty(String atributeType) throws Exception {
        Object obj = this.getProperty(atributeType);
        if (obj != null) {
            return (listToStringArray(obj));
        }
        return (null);
    }

    /**
     * 根据键值获取用户属性，返回ArrayList数组。（新增）
     *
     * @param atributeType
     * @return ArrayList
     * @throws Exception
     */
    public ArrayList<String> getArrayListProperty(String atributeType) throws Exception {
        Object obj = this.getProperty(atributeType);
        if (obj != null) {
            return (listToArrayList(obj));
        }
        return (null);
    }

    /**
     * 根据键值获取用户属性，返回String类型，如果有多值只返回第一个值。
     *
     * @param atributeType
     * @return String
     * @throws Exception
     */
    public String getFirstProperty(String atributeType) throws Exception {
        String[] props = getStringArrayProperty(atributeType);
        if (props == null || props.length == 0) {
            return (null);
        }
        if (props.length >= 1) {
            return (props[0]);
        }
        return (null);
    }

    /**
     * 根据键值获取用户属性，返回byte[]对象，只适用与单值属性。
     *
     * @param attributeType
     * @return byte[]
     * @throws Exception
     */
    public byte[] getBytesProperty(String attributeType) throws Exception {
        Object obj = this.getProperty(attributeType);
        if (obj == null) {
            return (null);
        } else if (obj instanceof byte[]) {
            return ((byte[]) obj);
        } else if (obj instanceof String) {
            return (((String) obj).getBytes());
        } else {
            throw (new IllegalStateException(
                "Invalid Attribute in LDAP, and this property cannot be convet to byte[]"));
        }

    }

    /**
     * 获取用户ID
     *
     * @return String
     */
    public String getUid() {
        return uid;
    }

    /**
     * 设置用户ID
     *
     * @param uid
     */
    public void setUid(String uid) {
        this.uid = uid;
    }

    /**
     * 获取用户类型
     *
     * @return String
     */
    public String getUserType() {
        return userType;
    }

    /**
     * 设置用户部门名称，根据语言类型langType来设置对应的部门名称，
     * langType的取值为，中文：UserInfoBean.KEY_LANG_CN；英文：UserInfoBean.KEY_LANG_EN。
     *
     * @param langType
     * @param value
     */
    public void setDepartmentName(String langType, String value) {
        if (departmentName == null) {
            departmentName = new ArrayList<>(3);
        }
        departmentName.add(langType + "=" + value);
    }

    /**
     * 设置用户显示名称，根据语言类型来设置对应的显示名称，
     * langType的取值为，中文：UserInfoBean.KEY_LANG_CN；英文：UserInfoBean.KEY_LANG_EN。
     *
     * @param langType
     * @param value
     */
    public void setDisplayName(String langType, String value) {
        if (displayName == null) {
            displayName = new ArrayList<>(3);
        }
        displayName.add(langType + "=" + value);
    }

    /**
     * 添加用户EMAIL信息
     *
     * @param email
     */
    public void addEmail(String email) {
        if (this.email == null) {
            this.email = new ArrayList<>(3);
        }
        this.email.add(email);
    }

    /**
     * 设置用户属性信息，多属性设置
     *
     * @param properties
     */
    public void setProperties(HashMap<String, Object> properties) {
        this.properties = properties;
    }

    /**
     * 设置用户属性信息，单属性设置
     *
     * @param key
     * @param value
     */
    public void setProperties(String key, Object value) {
        if (properties == null) {
            properties = new HashMap<>();
        }
        properties.put(key, value);
    }

    /**
     * 获取用户所有属性
     *
     * @return HashMap
     */
    public HashMap<String, Object> getAllProperties() {
        return (properties);
    }

    /**
     * 清空Properties中用户属性信息的值
     */
    public void clearProperty() {
        if (properties != null) {
            properties.clear();
        }
    }

    /**
     * 获取用户密码
     *
     * @return byte[]
     */
    public byte[] getUserPassword() {
        return ((byte[]) properties.get(KEY_USERPASSWORD));
    }

    /**
     * 从ArrayList中获取指定语言的属性值，返回值是不包括cn=或en=的纯属性值。
     *
     * @param array
     * @param lang
     * @return String
     */
    private String getLangPropertyFromArray(ArrayList<String> array, String lang) {
        if (!KEY_LANG_CN.equals(lang) && !KEY_LANG_EN.equals(lang)) {
            throw (new RuntimeException("无效的语言参数类型"));
        }
        if (array == null) {
            return (null);
        }
        for (int i = 0; i < array.size(); i++) {
            String tmp = array.get(i);
            if (tmp != null && tmp.startsWith(lang + "=")) {
                return (tmp.substring(lang.length() + 1));
            }
        }

        return (null);
    }

    /**
     * 获取工号
     *
     * @return String
     */
    public String getSn() {
        return sn;
    }

    /**
     * 设置工号
     *
     * @param sn
     */
    public void setSn(String sn) {
        this.sn = sn;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("dn:").append(this.dn).append('\n');
        buffer.append("uid:").append(this.uid).append('\n');
        buffer.append("cn:").append(this.cn).append('\n');
        buffer.append("sn:").append(this.sn).append('\n');
        buffer.append("givenName:").append(this.givenName).append('\n');
        buffer.append("departmentNumber:").append(this.departmentNumber).append('\n');
        buffer.append("employeeType:").append(this.employeeType).append('\n');
        buffer.append("employeeNumber:").append(this.employeeNumber).append('\n');
        buffer.append("email:").append(this.email).append('\n');
        buffer.append("displayName:").append(this.displayName).append('\n');
        buffer.append("departmentName:").append(this.departmentName).append('\n');
        buffer.append("userType:").append(this.userType).append('\n');
        buffer.append("groups").append(Arrays.toString(this.groups)).append('\n');
        buffer.append("createTimeStamp").append(createTimeStamp).append('\n');
        buffer.append("modifyTimeStamp").append(modifyTimeStamp).append('\n');
        buffer.append("preferredlanguage").append(preferredlanguage).append('\n');
        buffer.append(properties);
        return buffer.toString();
    }

    /**
     * 获取创建时间
     *
     * @return String
     */
    public String getCreateTimeStamp() {
        return createTimeStamp;
    }

    /**
     * 获取最后修改时间
     *
     * @return String
     */
    public String getModifyTimeStamp() {
        return modifyTimeStamp;
    }

    /**
     * 获取用户组，其中的值是带cn=的，如有必要需自己在代码中处理。
     *
     * @return String[]
     */
    public String[] getGroups() {
        if (groups == null) {
            return (new String[0]);
        }
        return groups;
    }

    /**
     * 设置用户组信息
     *
     * @param strings
     */
    public void setGroups(String[] strings) {
        groups = strings;
    }

    /**
     * 获取语言
     *
     * @return String
     */
    public String getPreferredlanguage() {
        return preferredlanguage;
    }

    /**
     * 设置语言
     *
     * @param string
     */
    public void setPreferredlanguage(String string) {
        preferredlanguage = string;
    }

    /**
     * 获取帐号过期提醒天数
     *
     * @return int
     */
    public int getAcctAlertDays() {
        return acctAlertDays;
    }

    /**
     * 设置帐号过期提醒天数
     *
     * @param i
     */
    public void setAcctAlertDays(int i) {
        acctAlertDays = i;
    }

    /**
     * 获取密码过期提醒天数
     *
     * @return int
     */
    public int getPwdAlertDays() {
        return pwdAlertDays;
    }

    /**
     * 设置密码过期提醒天数
     *
     * @param i
     */
    public void setPwdAlertDays(int i) {
        pwdAlertDays = i;
    }

    public String getNameId() {
        return nameId;
    }

    public void setNameId(String nameId) {
        this.nameId = nameId;
    }

}
