package com.bizfns.services.Controller;
public class EndpointPropertyKey {
    public static final String OTP_AUTHENTICATION = "/otpVerification";
    public static final String CHANGE_PASSWORD_SEND_OTP = "/changePasswordSendOtp";
    public static final String VALIDATE_OTP_AND_CHANGE_PASSWORD = "/validateOtpAndChangePassword";
    public static final String ADD_CUSTOMER = "/addCustomer";
    public static final String FETCH_PRE_REGISTRATION = "/fetchPreRegistration";
    public static final String PRE_CREATION_INFO = "/preCreationInfo";
    public static final String COMPANY_REGISTRATION = "/companyRegistration";
    public static final String PHONE_NUMBER_REGISTRATION_CHECK = "/phoneNoRegCheck";
    public static final String USER_LOGIN = "/api/users/userlogin";
    public static final String APP_LINK= "/appLink";
    public static final String GET_SECURITY_QUESTION = "/getSecurityQuestion";
    public static final String SAVE_SECURITY_QUESTION = "/saveSecurityQuestion";
    public static final String FORGOT_PASSWORD = "/forgotPassword";
    public static final String VALIDATE_FORGOT_PASSWORD_OTP = "/validateForgotPasswordOtp";
    public static final String RESET_PASSWORD = "/resetPassword";
    public static final String TEST_SCHEMA = "/testSchema";
    public static final String VERIFY_SECURITY_QUESTION = "/verifySecurityQuestion";
    public static final String UPDATE_BUSINESS_MOBILE_NO = "/updateBusinessMobileNo";
    public static final String ADD_STAFF= "/addStaff";
    public static final String PRE_STAFF_CREATION_DETAILS = "/preStaffCreationDetails";
    public static final String FETCH_CUSTOMER_LIST = "/fetchCustomerList";
    public static final String FETCH_STAFF_LIST = "/fetchStaffList";
    public static final String PRE_REGISTRATION_SEND_OTP = "/preregistrationSendOtp";
    public static final String PRE_REGISTRATION_OTP_VERIFICATION = "/preregistrationOtpVerification";
    public static final String TEST_FOT = "/testFor";
    public static final String PRE_NEW_SCHEDULE_DATA = "/preNewScheduleData";
    public static final String SERVICELIST = "/fetchServiceList";
    public static final String TST = "/tst";
    public static final String FORGOT_BUSINESS_ID = "/forgotBusinessId";
    public static final String FETCH_SERVICE_RATE_UNIT = "/fetchServiceRateUnit";
    public static final String ADD_SERVICE = "/addService";
    public static final String ADD_NEW_SCHEDULE = "/addNewSchedule";
    public static final String ADD_MATERIAL = "/addMaterial";
    public static final String MATERIAL_LIST = "/materialList";
    public static final String MATERIAL_CATEGORY_DATA = "/materialCategoryData";
    public static final String JOB_DETAILS = "/jobDetails";
    public static final String getjobnumberbydate = "/api/users/getJobNumberByDate";
    public static final String DELETE_SCHEDULE = "/api/users/deleteSchedule";
    public static final String CUSTOMER_HISTORY_RECORD_LIST = "/customerHistoryRecordList";
    public static final String SCHEDULE_LIST = "/api/users/scheduleList";
    public static final String TEST_ERROR_LOG = "/testErrorLog";
    public static final String SCHEDULE_HISTORY = "/scheduleHistory";
    public static final String RESCHEDULE_JOB = "/api/users/reScheduleJob";
    public static final String OTP_UPDATE_BUSINESS_EMAIL = "/otpUpdateBusinessEmail";
    public static final String VALIDATE_OTP_UPDATE_BUSINESS_EMAIL = "/validateOtpUpdateBusinessEmail";
    public static final String GET_MATERIAL_UNIT = "/api/getMaterialUnit";
    public static final String SAVE_MATERIAL_UNIT = "/api/saveMaterialUnit";
    public static final String ADD_SCHEDULE_NEW = "/addScheduleNew";
    public static final String EDIT_SCHEDULE = "/api/users/editSchedule";
    public static final String ADD_WORKING_HOURS = "/api/users/addWorkingHours";
    public static final String GET_WORKING_HOURS = "/api/users/getWorkingHours";
    public static final String SERVICE_ENTITY_FIELD = "/api/users/serviceEntityField";
    public static final String SCHEDULE_TIME_INTERVAL_SAVE = "/api/users/saveTimeInterval";
    public static final String GET_SCHEDULE_TIME_INTERVAL = "/api/users/getTimeInterval";
    public static final String GET_Job_price = "/api/users/getJobPrice";
    public static final String CUST_WISE_SERVICE_ENTITY = "/api/users/custWiseServiceEntity";
    public static final String GET_SERVICE_ENTITY_DETAILS = "/api/users/getServiceEntityDetails";
    public static final String GET_SERVICE_ENTITY_DETAILS_customerId = "/api/users/getServiceEntityDetailsBycustomerId";

    //create the in voice as per the job price details
    public static final String CREATE_INVOICE = "/api/users/createInvoice";
   //for bizfns admin panel
    public static final String Get_All_Schemas = "/api/users/getRegisterBusinessName";
    public static final String save_All_Successfully_Payments = "/api/users/getRegisterBusinessName";
    //get the invoice as per the name
    public static final String download_Invoice_file = "/api/users/downloadInvoiceFile/{invoice}";
    public static final String Get_All_recurrDate = "/getRecurrdate";
    public static final String Get_staff_checked_recurrDate = "/staffvalidateRecurrDate";
    public static final String addServiceEnity = "/addServiceEntity";
    public static final String customerHistory = "/customerHistory";
// for Image API(05-03-2024)
     public static final String save_Media_file = "/api/users/saveMediafile";
     public static final String get_Media_file = "/api/users/getMediafile";
     public static final String delete_Media_file = "/api/users/deleteMediafile";

    public static final String get_customer_service_history = "/api/users/getCustomerServiceHistory";
    public static final String download_Media_file = "/api/users/downloadMediafile/{imageName}";

    public static final String staff_change_password_using_temporary_password="/api/users/staffUserLogin";

    public static final String save_job_status="/api/users/saveJobStatus";

    public static final String get_job_status="/api/users/getJobStatus";

    public static final String get_profile="/getProfile";

    public static final String save_master_profile="/saveMasterProfile";

    public static final String get_otp_for_mobile_changes="/getOtpForMobileChanges";

    public static final String verify_password="/verifyPassword";

    public static final String upload_business_logo="/uploadBusinessLogo";

    public static final String save_Max_Job_Task="/api/users/saveMaxJobTask";

    public static final String get_MAx_Job_Task="/api/users/getMaxJobTask";
    public static final String CLIENT_LIST_BY_COMPANY_BUSINESS_NAME = "/getClientListByCompanyBusinessName";

    public static final String save_user_priviledges = "api/users/save_user_priviledges";

    public static final String get_assigned_priviledges = "api/users/get_assigned_priviledges";

    public static final String get_active_status_for_staff = "/getActiveStatusForStaff";

    public static final String update_active_inactive_status_for_staff = "/updateActiveInactiveStatusForStaff";

    public static final String delete_staff = "/deleteStaff";

    public static final String get_staff_details = "/getStaffDetails";
    public static final String update_staff_details = "/updateStaffDetails";

    public static final String get_userType_And_UserInfo = "/getUserTypeAndUserInfo";
    public static final String getNotificationMaster = "/getNotificationMaster";
    public static final String saveNotificationMaster = "/saveNotificationMaster";
    public static final String delete_material = "/deleteMaterial";

    public static final String get_Material_Details = "/getMaterialDetails";

    public static final String update_Material_Details = "/updateMaterialDetails";

    public static final String get_Customer_Details = "/getCustomerDetails";

    public static final String update_Customer_Details = "/updateCustomerDetails";

    public static final String delete_Customer = "/deleteCustomer";

    public static final String delete_Service_Object = "/api/users/deleteServiceObject";

    public static final String add_Material_Category = "/addMaterialCategory";

    public static final String add_Material_Sub_Category = "/addMaterialSubCategory";

    public static final String delete_category_And_Sub_Category = "/deleteCategoryAndSubcategory";

    public static final String update_Service_Object_Details = "/updateServiceObjectDetails";

    public static final String SaveEdit_InvoiceValues_By_JobIdAndCustomerIds = "/api/users/SaveEditInvoiceValuesByJobIdAndCustomerIds";

    public static final String UpdateEdit_InvoiceValues_By_CustomerId = "/api/users/updateEditInvoiceValues";

    public static final String GetEdit_InvoiceValues_By_JobIdAndCustomerIds = "/api/users/getEditInvoiceValuesByJobIdAndCustomerId";

    public static final String Create_Invoice_Pdf_By_Customers = "/api/users/CreateInvoicePdfByCustomers";

    public static final String saveTimeSheet = "/api/users/saveTimeSheet";

    public static final String updateTimeSheet = "/api/users/updateTimeSheet";

    public static final String getTimeSheetList = "/api/users/getTimeSheetList";

    public static final String getTimeSheetByBillNoAndStaffId = "/api/users/TimeSheetbyBillNoAndStaffId";
    public static final String get_Active_Inactive_Status_For_Service = "/api/users/getActiveInactiveStatusForService";
    public static final String update_Active_Inactive_Status_For_Service = "/api/users/UpdateActiveInactiveStatusForService";
    public static final String get_Active_Inactive_Status_For_Material = "/getActiveInactiveStatusForMaterial";
    public static final String update_Active_Inactive_Status_For_Material = "/UpdateActiveInactiveStatusForMaterial";

    public static final String get_Active_Inactive_Status_For_Customer = "/getActiveInactiveStatusForCustomer";
    public static final String update_Active_Inactive_Status_For_Customer = "/UpdateActiveInactiveStatusForCustomer";
    public static final String GET_SERVICE_DETAILS = "/getServiceDetails";
    public static final String UPDATE_SERVICE_DETAILS = "/updateServiceDetails";
    public static final String DELETE_SERVICE = "/deleteService";
    public static final String ADD_TAX_TABLE = "/addTaxTable";

    public static final String GET_TAX_TABLE = "/getTaxTable";

    public static final String UPDATE_TAX_TABLE = "/updateTaxTable";

    public static final String DELETE_TAX_TABLE = "/deleteTaxTable";

    public static final String GET_INVOICELISTS_BY_JOBID = "/api/users/getInvoiceListsByJobId";

    public static final String ADD_MATERIAL_CATEGORY = "/addMaterialCategory";

    public static final String ADD_MATERIAL_SUB_CATEGORY = "/addMaterialSubCategory";

    public static final String DELETE_MATERIAL_CATEGORY_SUBCATEGORY = "/deleteCategoryAndSubcategory";


}


