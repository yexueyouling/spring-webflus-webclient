/**  
 * Project Name:s01cross  
 * File Name:B07Resources.java  
 * Package Name:com.zhisen.cross.controller  
 * Date:2019年10月15日下午4:58:38  
 * Copyright (c) 2019, zhisen-tec All Rights Reserved.  
 *  
 */  
package com.zhisen.cross.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;
import org.springframework.web.util.UriBuilder;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.zhisen.cross.service.B07Service;
import com.zhisen.cross.util.ConstantsUtil;
import com.zhisen.cross.util.MergeUtil;
import com.zhisen.cross.util.SecurityUserUtil;
import com.zhisen.cross.util.StringUtils;
import com.zhisen.cross.util.TransforArrayUtil;

import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

/**
 * <b>Description:</b><br> B07资源Controller
 * @author 吉庆
 * @version 1.0
 * @Note
 * <b>ProjectName:</b> s01cross
 * <br><b>PackageName:</b> com.zhisen.cross.controller
 * <br><b>ClassName:</b> B07Resources
 * <br><b>Date:</b> 2019年10月15日 下午4:58:38
 */
@RestController
@RequestMapping(value = "/b07")
public class B07Resources {

	/**
     * logger: 日志输出对象
     * @since JDK 1.8
     */
    private static final Logger logger = LoggerFactory.getLogger(B07Resources.class);
    
    /**
     * webClientUtil: 响应式远程调用客户端
     * @since JDK 1.8
     */
    private final WebClient.Builder webClientBuilder;
    
    private B07Service b07Service;
    
    /**
     * 
     * <b>Description:</b><br> 构造方法注入
     * @param webClientBuilder    响应式远程调用客户端
     * 	      b07Service		  人资Service
     * @Note
     * <b>Author:</b> 吉庆
     * <br><b>Date:</b> 2019年10月15日下午5:07:26
     * <br><b>Version:</b> 1.0
     */
    public B07Resources(Builder webClientBuilder, B07Service b07Service) {
		super();
		this.webClientBuilder = webClientBuilder;
		this.b07Service = b07Service;
	}
    
    /**
     * 
     * <b>Description:</b><br>根据项目编码查询模版的支出项
     * @param proNo 项目编码
     * @return
     * @Note
     * <b>Author:</b> 吉庆
     * <br><b>Date:</b> 2019年10月15日 下午5:51:08
     * <br><b>Version:</b> 1.0
     */
    @GetMapping(value = "/expends/proNo/{proNo}")
    public Map<String, Object> expendDataKeyList(@PathVariable String proNo) {
    	logger.info("根据项目编码查询模版的支出项expendDataKeyList方法开始执行，执行参数为： proNo={}", proNo);
    	Map<String, Object> resultMap = new HashMap<String, Object>();
    	try {
    		logger.info("b02查询项目下的支出项集合selectDataKeyByProNo方法开始执行，执行参数为： proNo={}", proNo);
    		Map<String, Object> b02Res = webClientBuilder
                    .build().get()
                    .uri("/b02template/templates/datakey/proNo/{proNo}", proNo)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + SecurityUserUtil.getCurrentToken())
                    .retrieve().bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                    }).block();
        	logger.info("b02查询项目下的支出项集合selectDataKeyByProNo方法执行完毕，返回结果为： {}", b02Res);
            if (b02Res == null || b02Res.get(ConstantsUtil.RESULTDATA) == null) {
                resultMap.put(ConstantsUtil.RESULTDATA, null);
                resultMap.put(ConstantsUtil.STATUS, ConstantsUtil.FAIL);
                resultMap.put(ConstantsUtil.MSG, "EM-101");
                logger.info("b02查询项目下的支出项集合selectDataKeyByProNo方法执行完毕，返回结果为： {}", resultMap);
                return resultMap;
            } else {
            	//调用b04成功
            	//b04返回的收支类型查询提报项集合
            	@SuppressWarnings("unchecked")
    			List<Map<String, Object>> lCodeList = (List<Map<String, Object>>) b02Res.get(ConstantsUtil.RESULTDATA);
            	if (lCodeList == null || lCodeList.isEmpty()) {
            		resultMap.put(ConstantsUtil.RESULTDATA, lCodeList);
                    resultMap.put(ConstantsUtil.STATUS, ConstantsUtil.SUCCESS);
                    resultMap.put(ConstantsUtil.MSG, "SM-101");
                    logger.info("b02查询项目下的支出项集合selectDataKeyByProNo方法执行完毕，返回结果为空集合： {}", lCodeList);
                    return resultMap;
            	}
            	//数据标准查询参数
            	Map<String, Object> a01ParamMap = new HashMap<>();
            	
            	a01ParamMap.put("lCodeList", lCodeList);
            	
            	logger.info("a01根据lCode查询对应父节点selectParentInfoByLCode方法开始执行，执行参数为： a01ParamMap={}", a01ParamMap);
            	Map<String, Object> parentReportItems = webClientBuilder
                        .build()
                        .post()
                        .uri("/a01system/datastandards/parentinfos")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .body(BodyInserters.fromObject(a01ParamMap))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + SecurityUserUtil.getCurrentToken())
                        .retrieve().bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                        }).block();
            	logger.info("a01根据lCode查询对应父节点selectParentInfoByLCode方法结束，返回结果为： {}", parentReportItems);
            	if (parentReportItems == null || parentReportItems.get(ConstantsUtil.RESULTDATA) == null) {
                    resultMap.put(ConstantsUtil.RESULTDATA, null);
                    resultMap.put(ConstantsUtil.STATUS, ConstantsUtil.FAIL);
                    resultMap.put(ConstantsUtil.MSG, "EM-101");
                    logger.info("a01根据lCode查询对应父节点selectParentInfoByLCode方法执行完毕，返回结果为： {}", resultMap);
                    return resultMap;
                } else {
                	//调用a01成功
                	//拼装结果 
                	Map<String, Object> tempMap = MergeUtil.getMergeResultNoPagination(b02Res, parentReportItems, "lCode", "lCode");
                	@SuppressWarnings("unchecked")
					List<Map<String, Object>> tempList = (List<Map<String, Object>>) tempMap.get(ConstantsUtil.RESULTDATA);
                	// 存放返回到前台的数据
                	List<Map<String, Object>> resultList = new ArrayList<Map<String,Object>>();
                	for (Map<String, Object> temp : tempList) {
                		Map<String, Object> lCodeMap = new HashMap<>();
                		lCodeMap.put("lCode", temp.get("lCode"));
                		lCodeMap.put("cnValue", temp.get("pCnValue") + "-" + temp.get("cnName"));
                		resultList.add(lCodeMap);
                	} 
                	resultMap.put(ConstantsUtil.RESULTDATA, resultList);
                	resultMap.put(ConstantsUtil.STATUS, ConstantsUtil.SUCCESS);
        			resultMap.put(ConstantsUtil.MSG, "SM-101");
                }
            }
		} catch (Exception e) {
			resultMap.put(ConstantsUtil.RESULTDATA, null);
			resultMap.put(ConstantsUtil.STATUS, ConstantsUtil.FAIL);
			resultMap.put(ConstantsUtil.MSG, "EM-101");
			logger.error("根据项目编码查询模版的支出项expendDataKeyList方法执行异常，错误信息为： {}", e.getMessage());
		}
    	logger.info("根据项目编码查询模版的支出项expendDataKeyList方法执行完毕，返回结果为： {}", resultMap);
    	return resultMap;
    }
    
	/**
     * 
     * <b>Description:</b><br>查询项目支撑的列表
     * @param paramMap  entryType:入职类型，
	 *      			companyOrSchool:所属机构/院校，
	 *      			orgId:组织部门id，
	 *      			personCode:用户编码
     * @return resultData: 数据集合
     *         total: 记录数
     *         rows: 记录集合
     *             jobName: 职务名称,
     *             code: 人员编码,
     *             itemCode: 支出项编码,
     *             createUserCode: 创建人编码,
     *             typeName: 支出名称,
     *             jobCode: 职务编码,
     *             remark: 备注,
     *             createUserName: 创建人名称,
     *             userName: 人员名称,
     *             version: 乐观锁,
     *             userCode: 人员编码,
     *             typeCode: 支出编码,
     *             modifierCode: 更新人编码,
     *             proNo: 项目编码,
     *             itemName: 支出项名称,
     *             modifyTime: 更新时间,
     *             createTime: 创建时间,
     *             payScale: 支付比例,
     *             name: 人员名称,
     *             payDay: 发薪日,
     *             id: 项目支撑id,
     *             proName: 项目名称,
     *             modifierName: 更新人名称,
     *             isDel: 删除标记
     *		   message：消息编码,
     *		   status:状态
     * @Note
     * <b>Author:</b> 吉庆
     * <br><b>Date:</b> 2019年10月16日 下午1:57:06
     * <br><b>Version:</b> 1.0
     */
    @GetMapping(value = "/projectbrace")
    public Map<String, Object> selectProjectBraceList(@RequestParam Map<String, Object> paramMap) {
    	logger.info("根据项目编码查询模版的支出项expendDataKeyList方法开始执行，执行参数为： paramMap={}", paramMap);
    	Map<String, Object> resultMap = new HashMap<>();
    	try {
    		logger.info("a02查询用户和职务selectPersonAndJob方法开始执行，执行参数为： paramMap={}", paramMap);
    		// 用搜索条件查用户列表，返回用户名和职务信息
    		Map<String, Object> a02Res = webClientBuilder
                    .build()
                    .get()
                    .uri(uriBuilder -> {
                        UriBuilder builder = uriBuilder.path("/a02permission/persons/personjob");
                        for (String key: paramMap.keySet()){
                            builder.queryParam(key, paramMap.get(key));
                        }
                        return builder.build();
                    })
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + SecurityUserUtil.getCurrentToken())
                    .retrieve().bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                    }).block();
    		logger.info("a02查询用户和职务selectPersonAndJob方法执行完毕，返回结果为： {}", a02Res);
    		// 判断查询信息是否成功
    		if (a02Res == null || a02Res.get(ConstantsUtil.RESULTDATA) == null) {
    			 resultMap.put(ConstantsUtil.RESULTDATA, null);
                 resultMap.put(ConstantsUtil.STATUS, ConstantsUtil.SUCCESS);
                 resultMap.put(ConstantsUtil.MSG, "SM-101");
                 logger.info("a02查询用户和职务selectPersonAndJob方法执行完毕，返回结果为： {}", resultMap);
                 return resultMap;
    		} else {
    			// 判断查询到的用户是否为空
    			@SuppressWarnings("unchecked")
				List<Map<String, Object>> userCodeList = (List<Map<String, Object>>) a02Res.get(ConstantsUtil.RESULTDATA);
            	if (userCodeList == null || userCodeList.isEmpty()) {
            		resultMap.put(ConstantsUtil.RESULTDATA, userCodeList);
                    resultMap.put(ConstantsUtil.STATUS, ConstantsUtil.SUCCESS);
                    resultMap.put(ConstantsUtil.MSG, "SM-101");
                    logger.info("b02查询项目下的支出项集合selectDataKeyByProNo方法执行完毕，返回结果为空集合： {}", resultMap);
                    return resultMap;
            	}
            	Map<String, Object> b07ParamMap = new HashMap<>();
            	for (Map<String, Object> temp : userCodeList) {
            		temp.put("userCode", temp.get("code"));
            	}
            	b07ParamMap.put("userCodeList", userCodeList);
            	b07ParamMap.putAll(paramMap);
            	
            	logger.info("b07查询项目支撑selectAllProjectBrace方法开始执行，执行参数为： b07ParamMap={}", b07ParamMap);
            	Map<String, Object> b07Res = webClientBuilder
                        .build()
                        .post()
                        .uri("/b07humanresource/projectbraces/condition")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .body(BodyInserters.fromObject(b07ParamMap))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + SecurityUserUtil.getCurrentToken())
                        .retrieve().bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                        }).block();
            	logger.info("b07查询项目支撑selectAllProjectBrace方法结束，返回结果为： {}", b07Res);
            	if (b07Res == null || b07Res.get(ConstantsUtil.RESULTDATA) == null) {
                    resultMap.put(ConstantsUtil.RESULTDATA, null);
                    resultMap.put(ConstantsUtil.STATUS, ConstantsUtil.SUCCESS);
                    resultMap.put(ConstantsUtil.MSG, "SM-101");
                    logger.info("b07查询项目支撑selectAllProjectBrace方法执行完毕，返回结果为： {}", resultMap);
                    return resultMap;
                }else{
                	// 拼接用户的职务等信息
                	resultMap = MergeUtil.getMergeResultLeftPagination(b07Res, a02Res, "userCode", "userCode");
                	resultMap.put(ConstantsUtil.STATUS, ConstantsUtil.SUCCESS);
                    resultMap.put(ConstantsUtil.MSG, "SM-101");
                }
    		}
		} catch (Exception e) {
			resultMap.put(ConstantsUtil.RESULTDATA, null);
			resultMap.put(ConstantsUtil.STATUS, ConstantsUtil.FAIL);
			resultMap.put(ConstantsUtil.MSG, "EM-101");
			logger.error("查询项目支撑的列表selectProjectBraceList方法执行异常，错误信息为： {}", e.getMessage());
		}
    	logger.info("查询项目支撑的列表selectProjectBraceList方法执行完毕，返回结果为： {}", resultMap);
		return resultMap;
    }
    
    /**
     * 
     * <b>Description:</b><br>添加项目支撑
     * @param paramMap  userCode:人员编码，
	 * 					userName:人员名称，
	 * 					projectBraceList:项目支撑管理集合
	 * 						proNo:项目编码,
	 *						proName:项目名称,
	 *						itemCode:支出项编码,
	 *						itemName:支出项名称,
	 *						typeCode:支付类型,
	 *						typeName:支付名称,
	 *						payScale:支付比例,
	 *						payDay:发薪日,
	 *						remark:备注
     * @return
     * @Note
     * <b>Author:</b> 吉庆
     * <br><b>Date:</b> 2019年10月16日 下午4:22:00
     * <br><b>Version:</b> 1.0
     */
    @PostMapping(value = "/projectbrace")
    public Map<String, Object> insertProjectBrace(@RequestBody Map<String, Object> paramMap) {
    	logger.info("添加项目支撑insertProjectBrace方法开始执行，执行参数为： paramMap={}", paramMap);
    	Map<String, Object> resultMap = new HashMap<>();
    	try {
    		// 用户编码
			String userCode = (String) paramMap.get("userCode");
			if (StringUtils.isEmpty(userCode)) {
				resultMap.put(ConstantsUtil.RESULTDATA, null);
				resultMap.put(ConstantsUtil.STATUS, ConstantsUtil.FAIL);
				resultMap.put(ConstantsUtil.MSG, "用户不能为空");
				logger.info("添加项目支撑insertProjectBrace方法执行异常，错误信息为： {}", resultMap);
				return resultMap;
			}
			// 调用a02查询用户和职务
			logger.info("a02查询用户和职务selectPersonAndJob方法开始执行，执行参数为： paramMap={}", paramMap);
    		// 用搜索条件查用户列表，返回用户名和职务信息
    		Map<String, Object> a02Res = webClientBuilder
                    .build()
                    .get()
                    .uri(uriBuilder -> {
                        UriBuilder builder = uriBuilder.path("/a02permission/persons/personjob");
                        builder.queryParam("personCode", userCode);
                        return builder.build();
                    })
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + SecurityUserUtil.getCurrentToken())
                    .retrieve().bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                    }).block();
    		logger.info("a02查询用户和职务selectPersonAndJob方法执行完毕，返回结果为： {}", a02Res);
    		// 判断查询信息是否成功
    		if (a02Res == null || a02Res.get(ConstantsUtil.RESULTDATA) == null) {
    			 resultMap.put(ConstantsUtil.RESULTDATA, null);
                 resultMap.put(ConstantsUtil.STATUS, ConstantsUtil.FAIL);
                 resultMap.put(ConstantsUtil.MSG, "EM-101");
                 logger.info("a02查询用户和职务selectPersonAndJob方法执行完毕，返回结果为： {}", resultMap);
                 return resultMap;
    		} else {
    			// 判断查询到的用户是否为空
    			@SuppressWarnings("unchecked")
				List<Map<String, Object>> userCodeList = (List<Map<String, Object>>) a02Res.get(ConstantsUtil.RESULTDATA);
            	if (userCodeList == null || userCodeList.isEmpty()) {
            		resultMap.put(ConstantsUtil.RESULTDATA, userCodeList);
                    resultMap.put(ConstantsUtil.STATUS, ConstantsUtil.FAIL);
                    resultMap.put(ConstantsUtil.MSG, "EM-101");
                    logger.info("b02查询项目下的支出项集合selectDataKeyByProNo方法执行完毕，返回结果为空集合： {}", resultMap);
                    return resultMap;
            	}
            	
            	for (Map<String, Object> temp : userCodeList) {
            		paramMap.put("jobCode", temp.get("jobCode"));
            		paramMap.put("jobName", temp.get("jobName"));
            	}
            	
            	boolean delFlag = false;
            	boolean updateFlag = false;
            	boolean addFlag = false;
            	
            	@SuppressWarnings("unchecked")
				List<Map<String, Object>> delList = (List<Map<String, Object>>) paramMap.get("del");
            	if (delList != null && !delList.isEmpty()) {
            		delFlag = true;
            	}
            	@SuppressWarnings("unchecked")
				List<Map<String, Object>> updateList = (List<Map<String, Object>>) paramMap.get("update");
            	if (updateList != null && !updateList.isEmpty()) {
            		updateFlag = true;
            	}
            	@SuppressWarnings("unchecked")
				List<Map<String, Object>> addList = (List<Map<String, Object>>) paramMap.get("add");
            	if (addList != null && !addList.isEmpty()) {
            		addFlag = true;
            	}

            	int totalPayScale = 0;
            	// 合并添加和更新的数据
            	List<Map<String, Object>> payScaleList = new ArrayList<>();
            	payScaleList.addAll(addList);
            	payScaleList.addAll(updateList);
            	// 校验支付比例的和是否是100
            	for (Map<String, Object> temp : payScaleList) {
            		totalPayScale = totalPayScale + Integer.valueOf(String.valueOf(temp.get("payScale")));
            	}
            	
            	// 判断支付比例是否是100
            	if (totalPayScale != 100) {
            		resultMap.put(ConstantsUtil.RESULTDATA, null);
        			resultMap.put(ConstantsUtil.STATUS, ConstantsUtil.FAIL);
        			resultMap.put(ConstantsUtil.MSG, "EM-B07-118");
        			return resultMap;
            	}
            	
            	// 判断是否执行删除操作
            	if (delFlag) {
            		logger.info("b07删除项目支撑deleteProjectBrace方法开始执行，执行参数为： paramMap={}", paramMap);
            		Map<String, Object> b07DeleteRes = webClientBuilder
            				.build()
            				.put()
            				.uri("/b07humanresource/projectbraces/logicaldelete")
            				.contentType(MediaType.APPLICATION_JSON_UTF8)
            				.body(BodyInserters.fromObject(paramMap))
            				.header(HttpHeaders.AUTHORIZATION, "Bearer " + SecurityUserUtil.getCurrentToken())
            				.retrieve().bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
            				}).block();
            		logger.info("b07删除项目支撑deleteProjectBrace方法结束，返回结果为： {}", b07DeleteRes);
            		resultMap.putAll(b07DeleteRes);
            	}
            	
            	// 判断删除状态是否失败
            	if (String.valueOf(resultMap.get(ConstantsUtil.STATUS)).equals(ConstantsUtil.FAIL)) {
            		resultMap.put(ConstantsUtil.RESULTDATA, null);
        			resultMap.put(ConstantsUtil.STATUS, ConstantsUtil.FAIL);
        			resultMap.put(ConstantsUtil.MSG, "EM-101");
        			logger.info("b07添加项目支撑deleteProjectBrace方法结束，返回结果为： {}", resultMap);
        			return resultMap;
            	}
            	
            	// 判断是否执行更新操作
            	if (updateFlag) {
            		logger.info("b07更新项目支撑updateProjectBrace方法开始执行，执行参数为： paramMap={}", paramMap);
            		Map<String, Object> b07UpdateRes = webClientBuilder
            				.build()
            				.put()
            				.uri("/b07humanresource/projectbraces")
            				.contentType(MediaType.APPLICATION_JSON_UTF8)
            				.body(BodyInserters.fromObject(paramMap))
            				.header(HttpHeaders.AUTHORIZATION, "Bearer " + SecurityUserUtil.getCurrentToken())
            				.retrieve().bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
            				}).block();
            		logger.info("b07更新项目支撑updateProjectBrace方法结束，返回结果为： {}", b07UpdateRes);
            		resultMap.putAll(b07UpdateRes);
            	}
            	
            	// 判断更新状态是否失败
            	if (String.valueOf(resultMap.get(ConstantsUtil.STATUS)).equals(ConstantsUtil.FAIL)) {
            		resultMap.put(ConstantsUtil.RESULTDATA, null);
        			resultMap.put(ConstantsUtil.STATUS, ConstantsUtil.FAIL);
        			resultMap.put(ConstantsUtil.MSG, "EM-101");
        			logger.info("b07添加项目支撑deleteProjectBrace方法结束，返回结果为： {}", resultMap);
        			return resultMap;
            	}
            	
            	// 判断是否执行添加操作
            	if (addFlag) {
            		logger.info("b07添加项目支撑insertProjectBrace方法开始执行，执行参数为： paramMap={}", paramMap);
            		Map<String, Object> b07InsertRes = webClientBuilder
            				.build()
            				.post()
            				.uri("/b07humanresource/projectbraces")
            				.contentType(MediaType.APPLICATION_JSON_UTF8)
            				.body(BodyInserters.fromObject(paramMap))
            				.header(HttpHeaders.AUTHORIZATION, "Bearer " + SecurityUserUtil.getCurrentToken())
            				.retrieve().bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
            				}).block();
            		logger.info("b07添加项目支撑insertProjectBrace方法结束，返回结果为： {}", b07InsertRes);
            		resultMap.putAll(b07InsertRes);
            	}
            	
            	// 判断添加状态是否失败
            	if (String.valueOf(resultMap.get(ConstantsUtil.STATUS)).equals(ConstantsUtil.FAIL)) {
            		resultMap.put(ConstantsUtil.RESULTDATA, null);
        			resultMap.put(ConstantsUtil.STATUS, ConstantsUtil.FAIL);
        			resultMap.put(ConstantsUtil.MSG, "EM-101");
        			logger.info("b07添加项目支撑deleteProjectBrace方法结束，返回结果为： {}", resultMap);
        			return resultMap;
            	} 
    		}
		} catch (Exception e) {
			resultMap.put(ConstantsUtil.RESULTDATA, null);
			resultMap.put(ConstantsUtil.STATUS, ConstantsUtil.FAIL);
			resultMap.put(ConstantsUtil.MSG, "EM-101");
			logger.error("添加项目支撑insertProjectBrace方法执行异常，错误信息为： {}", e.getMessage());
		}
    	return resultMap;
    }
    
    
    /**
	 * 
	 * <b>Description:</b><br> 校验导入的工资excel数据
	 * @param 		file:待导入的工资excel
	 * @return		消息提示码，不是SM-101均有误
	 * @Note
	 * <b>Author:</b> 李晓旭
	 * <br><b>Date:</b> 2019年10月16日 下午3:29:56
	 * <br><b>Version:</b> 1.0
	 */
    @PostMapping(value="/inspectsalarydata")
    public Map<String, Object> checkSalaryData(@RequestParam String paramListStr, @RequestParam("file") MultipartFile file){
    	logger.info("校验导入的工资excel数据checkSalaryData方法开始执行，其他入参:{}，文件参数:{}", paramListStr,file);
    	Map<String, Object> resultMap = new HashMap<>(3);
    	
    	// 校验工资表数据和必填参数的参数map
    	Map<String, Object> b07ParamMap = new HashMap<>(2);
    	
    	// 解析前台传入json参数
		Map<String,Object> fileOtherParamMap = JSON.parseObject(paramListStr, new TypeReference<Map<String, Object>>() {});
    	// 除文件以外的其他参数
    	b07ParamMap.put("fileOtherParamMap", fileOtherParamMap);
    	
    	try {
    		// 待导入的工资excel数据集合
        	List<Map<String, Object>> salaryParamList = b07Service.readSalaryExecl(file);
        	
        	b07ParamMap.put("salaryParamList", salaryParamList);
        	
        	// 获取工资表中的人员编码集合
        	List<String> salaryUserCodeList = salaryParamList.stream().map(item -> String.valueOf(item.get("userCode"))).collect(Collectors.toList());
        	
        	// 校验同一个月份能发两边工资
        	logger.info("调用b07校验统一个月份工资不能导入两遍checkSalalyDate方法开始执行，传入参数：{}", fileOtherParamMap);
        	Map<String, Object> checkSalaryDateResult = webClientBuilder.build().get()
                    .uri(uriBuilder -> {
                        UriBuilder builder = uriBuilder.path("/b07humanresource/salarys/inspectsalarydate");
                        for (String key: fileOtherParamMap.keySet()){
                            builder.queryParam(key, fileOtherParamMap.get(key));
                        }
                        return builder.build();
                    }).header(HttpHeaders.AUTHORIZATION, "Bearer " + SecurityUserUtil.getCurrentToken())
                    .retrieve().bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                    }).block();
        	logger.info("调用b07校验统一个月份工资不能导入两遍checkSalalyDate方法结束，返回数据:{}", checkSalaryDateResult);
        	
        	if (!"SM-101".equals(checkSalaryDateResult.get(ConstantsUtil.MSG))) {
        		resultMap.putAll(checkSalaryDateResult);
        		logger.info("校验导入的工资excel数据checkSalaryData方法结束，返回数据:{}", resultMap);
        		return resultMap;
        	}
        	
        	logger.info("调用b07校验导入的工资excel数据checkSalaryExcelData方法开始执行，入参:{}", b07ParamMap);
    		Map<String, Object> b07ResultMap = webClientBuilder
                    .build()
                    .post()
                    .uri("/b07humanresource/salarys/inspectsalaryparam")
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .body(BodyInserters.fromObject(b07ParamMap))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + SecurityUserUtil.getCurrentToken())
                    .retrieve().bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                    }).block();
    		logger.info("调用b07校验导入的工资excel数据checkSalaryExcelData方法结束，返回数据:{}", b07ResultMap);
    		
    		// 数据必填校验成功
    		if ("SM-101".equals(b07ResultMap.get(ConstantsUtil.MSG))) {
    			// 调用a02查询人员和组织信息集合参数Map
    	    	Map<String, Object> a02ParamMap = new HashMap<>(1);
    			// 调用a02查询人员和组织信息集合，参数为工资人员编码集合
    	    	a02ParamMap.put("personCodeList", salaryUserCodeList);
    	    	
    	    	logger.info("调用a02查询人员和组织信息selectPersonAndOrgInfo方法开始执行，传入参数：{}", a02ParamMap);
    	    	Mono<Map<String, Object>> a02PersonAndOrgMap = webClientBuilder
                        .build()
                        .post()
                        .uri("/a02permission/persons/personandorginfo")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .body(BodyInserters.fromObject(a02ParamMap))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + SecurityUserUtil.getCurrentToken())
                        .retrieve().bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                        });
    	    	
    	    	// 调用a02查询人员组织入职类型为公司的相关信息参数map
    	    	Map<String, Object> a02CompanyTypeParam = new HashMap<>(2);
    	    	// 调用a02查询人员和组织信息集合，参数为工资人员编码集合
    	    	a02CompanyTypeParam.put("personCodeList", salaryUserCodeList);
    	    	// 补充参数入职类型为公司
    	    	a02CompanyTypeParam.put("type", ConstantsUtil.PERSON_COMPANY_ENTRY_TYPE);
    	    	
    	    	logger.info("调用a02查询入职为公司的人员和组织信息selectPersonAndOrgInfo方法开始执行，传入参数：{}", a02CompanyTypeParam);
    	    	Mono<Map<String, Object>> a02PerAndOrgAndCompanyMap = webClientBuilder
                        .build()
                        .post()
                        .uri("/a02permission/persons/personandorginfo")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .body(BodyInserters.fromObject(a02CompanyTypeParam))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + SecurityUserUtil.getCurrentToken())
                        .retrieve().bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                        });
    	    	
    	    	
    	    	
    	    	//发送两个请求，在结果返回后进行数据结果拼装
    	    	Tuple2<Map<String,Object>,Map<String,Object>> a02Result = 
    	    			Mono.zip(a02PersonAndOrgMap, a02PerAndOrgAndCompanyMap).block();
    	    	
    	    	// 根据导入的工资的人员编码查询的人员组织信息返回结果
    	    	Map<String, Object> personAndOrgResult = a02Result.getT1();
    	    	
    	    	// 根据导入的工资的人员编码和入职类型为公司的条件，查询人员、组织、公司信息返回的结果
    	    	Map<String, Object> personAndOrgAndCompanrResult = a02Result.getT2();
    	    	
    	    	logger.info("调用a02查询人员和组织信息selectPersonAndOrgInfo方法结束，返回数据:{}", personAndOrgResult);
    	    	logger.info("调用a02查询入职为公司的人员和组织信息selectPersonAndOrgInfo方法结束，返回数据:{}", personAndOrgAndCompanrResult);
    	    	
    	    	// 调用a02查询人员和组织信息集合成功
    	    	if ("SM-101".equals(personAndOrgResult.get(ConstantsUtil.MSG))) {
    	    		// 调用a02查询人员和组织信息集合,返回结果
    	    		@SuppressWarnings("unchecked")
					List<Map<String, Object>> personAndOrgList = 
					(List<Map<String, Object>>) personAndOrgResult.get(ConstantsUtil.RESULTDATA);
    	    		
    	    		// 获取人员信息中的人员id集合，查询人员扩展信息中的数据集合，调用c04mongo
    	    		List<String> personIdList = personAndOrgList.stream()
    	    				.map(item -> String.valueOf(item.get("id").toString())).collect(Collectors.toList());
    	    		
    	    		// 创建personId参数map
    	    		Map<String, Object> personIdParamMap = new HashMap<>(1);
    	    		
    	    		// c04mongo参数map赋值
    	    		personIdParamMap.put("personIdList", personIdList);
    	    		
    	    		// 获取入职类型为学院的学院id集合 
        	    	List<String> institutionIdList = personAndOrgList.stream().filter(item -> String.valueOf(item.get("entryType"))
        	    			.equals(ConstantsUtil.PERSON_SCHOOL_ENTRY_TYPE))
        	    			.map(item -> String.valueOf(item.get("companyOrSchool"))).collect(Collectors.toList());
        	    	
        	    	// b01查询合作院校参数map
        	    	Map<String, Object> b01InstitutionParamMap = new HashMap<>(1);
        	    	// b01查询合作院校参数map赋值
        	    	b01InstitutionParamMap.put("institutionIdList", institutionIdList);
    	    		
    	    		logger.info("调用c04根据人员id集合查询对应的人员扩展信息selectPersonExpByPersonIds方法开始执行，入参:{}", personIdParamMap);
    	    		Mono<Map<String, Object>> c04PersonExpResultMap = webClientBuilder
                            .build()
                            .post()
                            .uri("/c04mongo/personexps/personexpinfos")
                            .contentType(MediaType.APPLICATION_JSON_UTF8)
                            .body(BodyInserters.fromObject(personIdParamMap))
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + SecurityUserUtil.getCurrentToken())
                            .retrieve().bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                            });
        	    	
        	    	logger.info("调用b01根据合作院校id集合查询合作院校信息selectInstitutionInfoByIds方法开始执行,入参：{}",b01InstitutionParamMap);
        	    	Mono<Map<String, Object>> b01InstitutionResultMap = webClientBuilder
                            .build()
                            .post()
                            .uri("/b01business/institutions/institutioninfos")
                            .contentType(MediaType.APPLICATION_JSON_UTF8)
                            .body(BodyInserters.fromObject(b01InstitutionParamMap))
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + SecurityUserUtil.getCurrentToken())
                            .retrieve().bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                            });
        	    	
        	    	//发送两个请求，在结果返回后进行数据结果拼装
        	    	Tuple2<Map<String,Object>,Map<String,Object>> c04AndB01Result = 
        	    			Mono.zip(c04PersonExpResultMap, b01InstitutionResultMap).block();
        	    	// 人员扩展信息返回结果
        	    	Map<String, Object> c04PersonExpResult = c04AndB01Result.getT1();
        	    	// 人员入职的合作院校返回结果
        	    	Map<String, Object> b01InstitutionResult = c04AndB01Result.getT2();
        	    	logger.info("调用c04根据人员id集合查询对应的人员扩展信息selectPersonExpByPersonIds方法结束，返回数据:{}", c04PersonExpResult);
        	    	logger.info("调用b01根据合作院校id集合查询合作院校信息selectInstitutionInfoByIds方法结束，返回数据:{}", b01InstitutionResult);
        	    	
        	    	// 人员组织公司扩展信息集合
        	    	Map<String, Object> personAndOrgAndCompanyAndExpMap = new HashMap<>();
        	    	
        	    	// 人员组织和合作院校集合
        	    	Map<String, Object> personAndOrgAndInsMap = new HashMap<>();
        	    	
        	    	// 人员组织和合作院校扩展信息集合
        	    	Map<String, Object> personAndOrgAndInsAndExpMap = new HashMap<>();
        	    	
        	    	// 判断c04返回结果
        	    	if ("SM-101".equals(c04PersonExpResult.get(ConstantsUtil.MSG))) {
        	    		// c04返回数据集合
        	    		@SuppressWarnings("unchecked")
						List<Map<String, Object>> c04PersonExpList = (List<Map<String, Object>>) c04PersonExpResult.get(ConstantsUtil.RESULTDATA);
        	    		
        	    		// 转换id的类型
        	    		for (Map<String, Object> map : c04PersonExpList) {
        	    			Integer personId = Integer.valueOf(String.valueOf(map.get("personId")));
        	    			map.put("personId", personId);
						}
        	    		
        	    		// 合并入职到公司的人员、组织、公司、扩展信息
        	    		personAndOrgAndCompanyAndExpMap = MergeUtil.getMergeResultNoPagination(
        	    				personAndOrgAndCompanrResult,c04PersonExpResult, "id", "personId");
        	    		logger.info("人员组织公司扩展信息集合:{}", personAndOrgAndCompanyAndExpMap);
        	    	}else{
        	    		// 返回c04失败信息
        	    		resultMap.putAll(c04PersonExpResult);
        	    		logger.info("校验导入的工资excel数据checkSalaryData方法结束，返回数据:{}", resultMap);
        	    		return resultMap;
        	    	}
        	    	
        	    	
        	    	// 判断b01返回结果
        	    	if ("SM-101".equals(b01InstitutionResult.get(ConstantsUtil.MSG))) {
        	    		// 处理人员和组织的返回结果集合，去掉入职到公司的
        	    		List<Map<String, Object>> personAndOrgInsList = personAndOrgList.stream().filter(item -> String.valueOf(item.get("entryType"))
            	    			.equals(ConstantsUtil.PERSON_SCHOOL_ENTRY_TYPE)).collect(Collectors.toList());
        	    		
        	    		// 将过滤入职到学校的人员信息集合封装一下，该集合中只包含入职到学院的人员信息
        	    		Map<String, Object> personAndOrgInsResultMap = new HashMap<>(3);
        	    		personAndOrgInsResultMap.put(ConstantsUtil.RESULTDATA, personAndOrgInsList);
        	    		personAndOrgInsResultMap.put(ConstantsUtil.STATUS, ConstantsUtil.SUCCESS);
        	    		personAndOrgInsResultMap.put(ConstantsUtil.MSG, "SM-101");
        	    		
        	    		personAndOrgAndInsMap = MergeUtil.getMergeResultNoPagination(
        	    				personAndOrgInsResultMap,b01InstitutionResult, "companyOrSchool", "insId");
        	    		logger.info("人员组织和合作院校集合:{}", personAndOrgAndInsMap);
        	    		
        	    		personAndOrgAndInsAndExpMap = MergeUtil.getMergeResultNoPagination(
        	    				personAndOrgAndInsMap,c04PersonExpResult, "id", "personId");
        	    		logger.info("人员组织和合作院校和扩展信息集合:{}", personAndOrgAndInsAndExpMap);
        	    	}else{
        	    		// 返回b01失败信息
        	    		resultMap.putAll(b01InstitutionResult);
        	    		logger.info("校验导入的工资excel数据checkSalaryData方法结束，返回数据:{}", resultMap);
        	    		return resultMap;
        	    	}
        	    	
        	    	// 调用b07查询项目支撑
        	    	Map<String, Object> userCodeParamMap = new HashMap<>();
        	    	// 用户编码集合赋值
        	    	userCodeParamMap.put("userCodeList", salaryUserCodeList);
        	    	
        	    	// 根据导入的用户编码调用b07查询项目支撑
        	    	logger.info("调用b07根据人员编码集合查询对应的项目支撑selectProjectBraceByUserCodeList方法开始执行，入参:{}", userCodeParamMap);
        	    	Map<String, Object> proBraceResultMap = webClientBuilder
                            .build()
                            .post()
                            .uri("/b07humanresource/projectbraces/usercodeprojectbraces")
                            .contentType(MediaType.APPLICATION_JSON_UTF8)
                            .body(BodyInserters.fromObject(userCodeParamMap))
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + SecurityUserUtil.getCurrentToken())
                            .retrieve().bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                            }).block();
        	    	logger.info("调用b07根据人员编码集合查询对应的项目支撑selectProjectBraceByUserCodeList方法结束，返回数据:{}", proBraceResultMap);
        	    	// 判断调用b07查询项目支撑返回结果
        	    	if (!"SM-101".equals(proBraceResultMap.get(ConstantsUtil.MSG))) {
        	    		// 返回调用b07查询项目支撑失败信息
        	    		resultMap.putAll(proBraceResultMap);
        	    		logger.info("校验导入的工资excel数据checkSalaryData方法结束，返回数据:{}", resultMap);
        	    		return resultMap;
        	    	}
        	    	
        	    	
        	    	// 调用b07checkSalaryPersonData校验数据准确性Service,返回验证信息
        	    	Map<String, Object> checkMessageMap =b07Service.checkSalaryPersonData(salaryParamList, personAndOrgAndCompanyAndExpMap, 
        	    			personAndOrgAndInsAndExpMap, proBraceResultMap);
        	    	// 返回结果赋值
        	    	resultMap.putAll(checkMessageMap);
     	    	}else{
    	    		// 返回a02失败信息
    	    		resultMap.putAll(personAndOrgResult);
    	    		logger.info("校验导入的工资excel数据checkSalaryData方法结束，返回数据:{}", resultMap);
    	    		return resultMap;
    	    	}
    		}else{
    			// 返回b07数据必填校验失败
    			resultMap.putAll(b07ResultMap);
    			logger.info("校验导入的工资excel数据checkSalaryData方法结束，返回数据:{}", resultMap);
    			return resultMap;
    		}
		} catch (Exception e) {
			resultMap.put(ConstantsUtil.RESULTDATA, null);
			resultMap.put(ConstantsUtil.STATUS, ConstantsUtil.FAIL);
			resultMap.put(ConstantsUtil.MSG, "EM-101");
			logger.error("校验导入的工资excel数据checkSalaryData方法执行异常，错误信息为： {}", e.getMessage());
		}
    	logger.info("校验导入的工资excel数据checkSalaryData方法结束，返回数据:{}", resultMap);
    	return resultMap;
    }
    
    /**
     * 
     * <b>Description:</b><br> 导入工资
     * @param 		paramListStr:除文件以外的其他参数
     * 					salalyDate:工资年月，
     * 					ncCode:支付编码
     * @param 		file:待导入文件
     * @return		操作消息
     * @Note
     * <b>Author:</b> 李晓旭
     * <br><b>Date:</b> 2019年10月18日 上午10:36:24
     * <br><b>Version:</b> 1.0
     */
    @PostMapping(value="/salaryexcel")
    public Map<String, Object> importSalary(@RequestParam String paramListStr, @RequestParam("file") MultipartFile file){
    	logger.info("导入工资importSalary方法开始执行，其他入参:{}，文件参数:{}，文件名称：{}", paramListStr, file, file.getOriginalFilename());
    	// 解析前台传入json参数
		Map<String,Object> fileOtherParamMap = JSON.parseObject(paramListStr, new TypeReference<Map<String, Object>>() {});
		
		// 工资年月
		String salalyDate = String.valueOf(fileOtherParamMap.get("salalyDate"));
		
		// ncCode nc编码
		String ncCode = String.valueOf(fileOtherParamMap.get("ncCode"));
		
    	// 返回结果集合
    	Map<String, Object> resultMap = new HashMap<>(3);
    	try {
    		// 校验工资表数据和必填参数的参数map
        	Map<String, Object> importSalaryParamMap = new HashMap<>(2);
        	// 除文件以外的其他参数
        	importSalaryParamMap.put("salalyDate", salalyDate);
        	importSalaryParamMap.put("ncCode", ncCode);
        	
    		// 待导入的工资excel数据集合
        	List<Map<String, Object>> salaryParamList = b07Service.readSalaryExecl(file);
        	
        	importSalaryParamMap.put("salaryList", salaryParamList);
        	
        	// 获取工资表中的人员编码集合
        	List<String> salaryUserCodeList = salaryParamList.stream().map(item -> String.valueOf(item.get("userCode"))).collect(Collectors.toList());
        	
        	// 调用b07查询项目支撑，根据用户编码集合获取项目支撑
	    	Map<String, Object> userCodeParamMap = new HashMap<>();
	    	// 用户编码集合赋值
	    	userCodeParamMap.put("userCodeList", salaryUserCodeList);
	    	
	    	// 调用b04查询预算参数
	    	Map<String, Object> planParamMap = new HashMap<>();
	    	
	    	planParamMap.put("salalyDate", salalyDate);
	    	
	    	// 根据导入的用户编码调用b07查询项目支撑
	    	logger.info("调用b07根据人员编码集合查询对应的项目支撑selectProjectBraceByUserCodeList方法开始执行，入参:{}", userCodeParamMap);
	    	Mono<Map<String, Object>> projectBrace = webClientBuilder
                    .build()
                    .post()
                    .uri("/b07humanresource/projectbraces/usercodeprojectbraces")
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .body(BodyInserters.fromObject(userCodeParamMap))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + SecurityUserUtil.getCurrentToken())
                    .retrieve().bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                    });
	    	logger.info("调用b04根据发工资年度查询对应的所有项目最新的预算selectPlanBySalaryYear方法开始执行，执行参数为： paramMap={}", planParamMap);
	    	// 调用b04查询预算，根据年份
	    	Mono<Map<String, Object>> plan = webClientBuilder.build().get()
                    .uri(uriBuilder -> {
                        UriBuilder builder = uriBuilder.path("/b04budget/budgetplans/salaryplan");
                        for (String key: planParamMap.keySet()){
                            builder.queryParam(key, planParamMap.get(key));
                        }
                        return builder.build();
                    }).header(HttpHeaders.AUTHORIZATION, "Bearer " + SecurityUserUtil.getCurrentToken())
                    .retrieve().bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                    });
	    	
	    	//发送两个请求，调用b07查询项目支撑和调用b04查询最新预算
	    	Tuple2<Map<String,Object>,Map<String,Object>> projectBraceAndPlanResult = 
	    			Mono.zip(projectBrace, plan).block();
	    	
	    	// 项目支撑返回结果
	    	Map<String, Object> projectBraceResultMap = projectBraceAndPlanResult.getT1();
	    	// 预算返回结果
	    	Map<String, Object> planResultMap = projectBraceAndPlanResult.getT2();
	    	logger.info("调用b04根据发工资年度查询对应的所有项目最新的预算selectPlanBySalaryYear方法执行完毕，返回结果为： {}", planResultMap);
	    	logger.info("调用b07根据人员编码集合查询对应的项目支撑selectProjectBraceByUserCodeList方法结束，返回数据:{}", projectBraceResultMap);
	    	
	    	// 判断项目支撑的返回结果
	    	if (!"SM-101".equals(projectBraceResultMap.get(ConstantsUtil.MSG))) {
	    		resultMap.putAll(projectBraceResultMap);
	    		logger.info("导入工资importSalary方法结束，返回数据:{}", resultMap);
	    		return resultMap;
	    	}
	    	// 判断预算的返回结果
	    	if (!"SM-101".equals(planResultMap.get(ConstantsUtil.MSG))) {
	    		resultMap.putAll(planResultMap);
	    		logger.info("导入工资importSalary方法结束，返回数据:{}", resultMap);
	    		return resultMap;
	    	}
	    	
	    	// 调用b07Service中的组合提报数据service，处理项目支撑，组成添加提报数据的参数集合
	    	List<Map<String, Object>> reportDataParamList = b07Service.dealPlanReportData(salaryParamList,
	    			projectBraceResultMap, planResultMap, ncCode, salalyDate);
	    	
	    	// 调用b04添加提报数据参数
	    	Map<String, Object> reportDataParamMap = new HashMap<>(1);
	    	
	    	// 调用b04添加提报数据参数赋值
	    	reportDataParamMap.put("reportDataList", reportDataParamList);
	    	
        	// 同步发送导入工资和添加提报请求
	    	// 调用b07导入工资
        	logger.info("调用b07导入工资insertSalary方法开始执行，入参:{}", importSalaryParamMap);
        	Mono<Map<String, Object>> importSalary = webClientBuilder
                    .build()
                    .post()
                    .uri("/b07humanresource/salarys")
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .body(BodyInserters.fromObject(importSalaryParamMap))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + SecurityUserUtil.getCurrentToken())
                    .retrieve().bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                    });
        	
        	// 调用b04添加提报
        	logger.info("调用b04批量插入提报数据insertReportDataBatch方法开始执行，执行参数为： paramMap={}", reportDataParamMap);
        	Mono<Map<String, Object>> addReportData = webClientBuilder
                    .build()
                    .post()
                    .uri("/b04budget/budgetreports/salaryreportbatch")
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .body(BodyInserters.fromObject(reportDataParamMap))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + SecurityUserUtil.getCurrentToken())
                    .retrieve().bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                    });
        	
        	//发送两个请求，调用b07导入工资和调用b04添加提报
	    	Tuple2<Map<String,Object>,Map<String,Object>> importSalaryAndAddReportRes = 
	    			Mono.zip(importSalary, addReportData).block();
	    	
	    	// 调用b07导入工资返回结果
	    	Map<String, Object> importSalaryResult = importSalaryAndAddReportRes.getT1();
	    	logger.info("调用b07导入工资insertSalary方法结束，返回数据:{}", importSalaryResult);
	    	if (!"SM-101".equals(importSalaryResult.get(ConstantsUtil.MSG))) {
	    		resultMap.putAll(importSalaryResult);
	    		logger.info("导入工资importSalary方法结束，返回数据:{}", resultMap);
	    		return resultMap;
	    	}
	    	
	    	// 调用b04添加提报返回结果
	    	Map<String, Object> addReportDataResult = importSalaryAndAddReportRes.getT2();
	    	logger.info("调用b04批量插入提报数据insertReportDataBatch方法执行完毕，返回结果为： {}", addReportDataResult);
	    	if (!"SM-101".equals(addReportDataResult.get(ConstantsUtil.MSG))) {
	    		resultMap.putAll(addReportDataResult);
	    		logger.info("导入工资importSalary方法结束，返回数据:{}", resultMap);
	    		return resultMap;
	    	}
	    	
	    	// 如果导入工资和添加提报数据返回结果成功
	    	resultMap.putAll(importSalaryResult);
		} catch (Exception e) {
			resultMap.put(ConstantsUtil.RESULTDATA, null);
			resultMap.put(ConstantsUtil.STATUS, ConstantsUtil.FAIL);
			resultMap.put(ConstantsUtil.MSG, "EM-101");
			logger.error("导入工资importSalary方法执行异常，错误信息为： {}", e.getMessage());
		}
    	logger.info("导入工资importSalary方法结束，返回数据:{}", resultMap);
    	return resultMap;
    }
    
    
    /**
	 * 
	 * <b>Description:</b><br> 工资和人员信息的组合查询
	 * @param 	paramMap
	 * 				userCodeList:用户编码集合,
	 * 				salalyDate:工资年月,
	 * 				personName:用户名称,
	 *				entryType:入职类型,
	 * 				companyOrSchool:所属机构/院校Id,
	 * 				orgId:组织部门id,
	 * 				personCodeList:用户编码集合,
	 * 				personIdList:用户id集合
	 * @return
	 *			total: 总数
	 *     		rows: 	
	 * 				id:用户id,
	 * 				name:用户名称,
	 * 				entryType:入职类型,
	 * 				companyOrSchool:合作院校id或公司id,
	 * 				companyName:公司名称(如果入职类型为公司时有该字段),
	 * 				orgId:主职部门id,
	 * 				orgName:主职部门名称,
	 * 				jobCode:主职职务编码,
	 * 				jobName:主职职务名,				
	 * 				salaryId:工资主键id,			
	 * 				salaryId:工资主键id,
	 * 				userCode:人员工号 ,
	 * 				basicSalaly:固定薪资-基本工资 ,
	 * 				gobSalaly:固定薪资-岗位工资 ,
	 * 				secretSalaly:固定薪资-保密工资 ,
	 * 				subSidies:固定薪资-补贴 ,
	 * 				performance:浮动薪资-绩效 ,
	 * 				bonus:浮动薪资-奖金 ,
	 * 				floatOther:浮动薪资-其他 ,
	 * 				salalyTotal:工资总额 ,
	 * 				attendanceCode:考勤编码: MD26 ,
	 * 				attendanceName:考勤名称 ,
	 * 				otherAddReduce:其他增减 ,
	 * 				welfareTax:福利税 ,
	 * 				salalyPayment:应发工资 ,
	 * 				accumulationCompany:住房公积金-公司 ,
	 * 				accumulationPerson:住房公积金-个人 ,
	 * 				socialSecurityCompany:社保-公司 ,
	 * 				socialSecurityPerson:社保-个人 ,
	 * 				pertaxSalalyTotal:税前工资总额 ,
	 * 				beforeAccumulate:工资总额-之前积累 ,
	 * 				total:工资总额-总计 ,
	 * 				fsiaohfMonTotal:五险一金-当月合计 ,
	 * 				fsiaohfAccumulate:五险一金-之前累计 ,
	 * 				fsiaohfTotal:五险一金-总计 ,
	 * 				specialAddDeduction:专项附加扣除 ,
	 * 				costDeductionStandard:费用扣除标准 ,
	 * 				otherDeduction:其他扣除 ,
	 * 				taxTotal:征税总额 ,
	 * 				monPayableIncomeTax:当月应缴个税 ,
	 * 				yearPaidTax:当年已缴税额 ,
	 * 				monActualIncomeTax:当月实际个税 ,
	 * 				actualSalaly:实发工资 ,
	 * 				companyPayMoney:服务费-公司支付金额 ,
	 * 				deduction:服务费-扣款 ,
	 * 				rate:服务费-税率 ,
	 * 				taxation:服务费-税费 ,
	 * 			 	actualMoneyService:服务费-实发金额 ,
	 * 				other:其他 ,
	 * 				actualTotal:实发总计 ,
	 * 				agentCompanyServiceCost:代理公司服务成本 ,
	 * 				monPersonCostTotal:本月人员成本合计 ,
	 * 				lastMonPersonCostTotal:上月人员成本合计 ,
	 * 				differences:差异 ,
	 * 				userTypeCode:人员类型编码:成本（MD2501），费用（MD2502） ,
	 * 				userTypeName:人员类型名称 ,
	 * 				salalyDate:工资年月:年月 ,
	 * 				remark:备注 ,
	 * 				modiferCode:修改人编码,
	 * 				modiferName:修改人姓名,
	 * 				modifyTime:修改时间,
	 * 				createUserCode:创建人编码,
	 * 				createUserName:创建人名称,
	 * 				createTime:创建时间,
	 * 				version:乐观锁,
	 * 				isDel:删除标记：0：未删除，1：已删除
	 * @Note
	 * <b>Author:</b> 李晓旭
	 * <br><b>Date:</b> 2019年10月19日 下午1:49:05
	 * <br><b>Version:</b> 1.0
	 */
    @GetMapping(value="/salaryandpersonconditions")
    public Map<String, Object> selectSalaryAndPersonInfo(@RequestParam Map<String, Object> paramMap, HttpServletRequest request){
    	logger.info("工资和人员信息的组合查询selectSalaryAndPersonInfo方法开始执行，执行参数为： paramMap={}", paramMap);
		Map<String, Object> resultMap = new HashMap<>(3);
		try {
			List<Object> orgIdList = TransforArrayUtil.transforArray("orgId", request);
			if(orgIdList != null && !orgIdList.isEmpty()){
				paramMap.put("orgIdList", orgIdList);
			}
			// 获取参数中的入职类型
			String entryType = String.valueOf(paramMap.get("type"));
			// 工资人员最终返回结果集合
			Map<String, Object> salaryAndPersonResultMap = new HashMap<>();
			// 合作院校返回结果
			Map<String, Object> b01InstitutionResultMap = new HashMap<>();
			// 人员、组织、公司返回结果
			Map<String, Object> perAndOrgAndCompanyResultMap = new HashMap<>();
			
			// 先请求a02人员信息然后过滤b07工资信息
			// 调用b02查询人员和组织信息
        	logger.info("调用b02查询人员和组织信息selectPersonAndOrgInfo方法开始执行，执行参数为： paramMap={}", paramMap);
        	Map<String, Object> personAndOrgResultMap = webClientBuilder
                    .build()
                    .post()
                    .uri("/a02permission/persons/personandorginfo")
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .body(BodyInserters.fromObject(paramMap))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + SecurityUserUtil.getCurrentToken())
                    .retrieve().bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                    }).block();
        	
        	// 取调用b02查询人员和组织信息返回结果，进行判断
	    	logger.info("调用b02查询人员和组织信息selectPersonAndOrgInfo方法结束，返回数据:{}", personAndOrgResultMap);
	    	if (!"SM-101".equals(personAndOrgResultMap.get(ConstantsUtil.MSG))) {
	    		resultMap.putAll(personAndOrgResultMap);
	    		logger.info("工资和人员信息的组合查询selectSalaryAndPersonInfo方法结束，返回数据:{}", resultMap);
	    		return resultMap;
	    	}
	    	
	    	// 获取查询人员集合
	    	@SuppressWarnings("unchecked")
			List<Map<String, Object>> personAndOrgDataList = (List<Map<String, Object>>) personAndOrgResultMap.get(ConstantsUtil.RESULTDATA);
	    	// 人员信息为空
	    	if (personAndOrgDataList == null || personAndOrgDataList.isEmpty()) {
	    		Map<String, Object> personAndOrgNullDataMap = new HashMap<>();
	    		personAndOrgNullDataMap.put(ConstantsUtil.ROWS, personAndOrgDataList);
	    		resultMap.put(ConstantsUtil.RESULTDATA, personAndOrgNullDataMap);
				resultMap.put(ConstantsUtil.STATUS, ConstantsUtil.SUCCESS);
				resultMap.put(ConstantsUtil.MSG, "SM-101");
				logger.info("工资和人员信息的组合查询selectSalaryAndPersonInfo方法结束，返回数据:{}", resultMap);
	    		return resultMap;
	    	}
			
	    	// 获取查询后的用户编码集合
	    	List<String> userCodeList = personAndOrgDataList.stream()
	    			.map(item -> String.valueOf(item.get("code"))).collect(Collectors.toList());
	    	
	    	paramMap.put("userCodeList", userCodeList);
	    	
			// 调用b07查询工资信息，带有分页
			logger.info("调用b07动态条件查询工资selectSalary方法开始执行，入参:{}", paramMap);
        	Map<String, Object> salaryResultMap = webClientBuilder
                    .build()
                    .post()
                    .uri("/b07humanresource/salarys/conditions")
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .body(BodyInserters.fromObject(paramMap))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + SecurityUserUtil.getCurrentToken())
                    .retrieve().bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                    }).block();
        	
	    	// 取调用b07查询工资返回结果，进行判断
	    	logger.info("调用b07动态条件查询工资selectSalary方法结束，返回数据:{}", salaryResultMap);
	    	if (!"SM-101".equals(salaryResultMap.get(ConstantsUtil.MSG))) {
	    		resultMap.putAll(salaryResultMap);
	    		logger.info("工资和人员信息的组合查询selectSalaryAndPersonInfo方法结束，返回数据:{}", resultMap);
	    		return resultMap;
	    	}
	    	
	    	// 取b07查询工资返回结果集合（Map）
	    	@SuppressWarnings("unchecked")
			Map<String, Object> salaryDataResultMap = (Map<String, Object>) salaryResultMap.get(ConstantsUtil.RESULTDATA);
	    	// 取工资的rows集合
	    	@SuppressWarnings("unchecked")
			List<Map<String, Object>> salaryRows = (List<Map<String, Object>>) salaryDataResultMap.get(ConstantsUtil.ROWS);
	    	// 工资返回数据为空，直接返回
	    	if (salaryRows == null || salaryRows.isEmpty()) {
	    		resultMap.putAll(salaryResultMap);
	    		logger.info("工资和人员信息的组合查询selectSalaryAndPersonInfo方法结束，返回数据:{}", resultMap);
	    		return resultMap;
	    	}
	    	
	    	// 处理人员的隶属公司（学院或者公司）和人员的扩展信息
	    	// 调用a02查询人员和组织信息集合,返回结果
    		@SuppressWarnings("unchecked")
			List<Map<String, Object>> personAndOrgList = 
			(List<Map<String, Object>>) personAndOrgResultMap.get(ConstantsUtil.RESULTDATA);
    		
    		// 获取人员信息中的人员id集合，查询人员扩展信息中的数据集合，调用c04mongo
    		List<String> personIdList = personAndOrgList.stream()
    				.map(item -> String.valueOf(item.get("id").toString())).collect(Collectors.toList());
    		
    		// 创建personId参数map
    		Map<String, Object> personIdParamMap = new HashMap<>(1);
    		
    		// c04mongo参数map赋值
    		personIdParamMap.put("personIdList", personIdList);
    		
    		logger.info("调用c04根据人员id集合查询对应的人员扩展信息selectPersonExpByPersonIds方法开始执行，入参:{}", personIdParamMap);
    		Map<String, Object> c04PersonExpResultMap = webClientBuilder
                    .build()
                    .post()
                    .uri("/c04mongo/personexps/personexpinfos")
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .body(BodyInserters.fromObject(personIdParamMap))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + SecurityUserUtil.getCurrentToken())
                    .retrieve().bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                    }).block();
    		logger.info("调用c04根据人员id集合查询对应的人员扩展信息selectPersonExpByPersonIds方法结束，返回数据:{}", c04PersonExpResultMap);
    		
    		if (!"SM-101".equals(c04PersonExpResultMap.get(ConstantsUtil.MSG))) {
	    		resultMap.putAll(c04PersonExpResultMap);
	    		logger.info("工资和人员信息的组合查询selectSalaryAndPersonInfo方法结束，返回数据:{}", resultMap);
	    		return resultMap;
	    	}else{
	    		// c04返回数据集合
	    		@SuppressWarnings("unchecked")
				List<Map<String, Object>> c04PersonExpList = (List<Map<String, Object>>) c04PersonExpResultMap.get(ConstantsUtil.RESULTDATA);
	    		
	    		// 转换id的类型
	    		for (Map<String, Object> map : c04PersonExpList) {
	    			Integer personId = Integer.valueOf(String.valueOf(map.get("personId")));
	    			map.put("personId", personId);
				}
	    	}
    		
    		// 如果查询条件入职类型不是公司的时候需要查询人员入职类型为合作院校（type 不是  MD2201）
    		if (!ConstantsUtil.PERSON_COMPANY_ENTRY_TYPE.equals(entryType)) {
    			// 获取入职类型为学院的学院id集合 
    	    	List<String> institutionIdList = personAndOrgList.stream().filter(item -> String.valueOf(item.get("entryType"))
    	    			.equals(ConstantsUtil.PERSON_SCHOOL_ENTRY_TYPE))
    	    			.map(item -> String.valueOf(item.get("companyOrSchool"))).collect(Collectors.toList());
    	    	
    	    	// b01查询合作院校参数map
    	    	Map<String, Object> b01InstitutionParamMap = new HashMap<>(1);
    	    	// b01查询合作院校参数map赋值
    	    	b01InstitutionParamMap.put("institutionIdList", institutionIdList);
        		
    	    	logger.info("调用b01根据合作院校id集合查询合作院校信息selectInstitutionInfoByIds方法开始执行,入参：{}",b01InstitutionParamMap);
    	    	b01InstitutionResultMap = webClientBuilder
                        .build()
                        .post()
                        .uri("/b01business/institutions/institutioninfos")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .body(BodyInserters.fromObject(b01InstitutionParamMap))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + SecurityUserUtil.getCurrentToken())
                        .retrieve().bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                        }).block();
    	    	logger.info("调用b01根据合作院校id集合查询合作院校信息selectInstitutionInfoByIds方法结束，返回数据:{}", b01InstitutionResultMap);
    	    	
    	    	if (!"SM-101".equals(b01InstitutionResultMap.get(ConstantsUtil.MSG))) {
    	    		resultMap.putAll(b01InstitutionResultMap);
    	    		logger.info("工资和人员信息的组合查询selectSalaryAndPersonInfo方法结束，返回数据:{}", resultMap);
    	    		return resultMap;
    	    	}
    		}
    		
    		// 如果参数入职类型为null，在这再查询一遍入职类型为公司的，需要查询到隶属公司
	    	if (StringUtils.isEmpty(entryType)) {
	    		// 调用a02查询人员组织入职类型为公司的相关信息参数map
    	    	// 补充参数入职类型为公司
    	    	paramMap.put("type", ConstantsUtil.PERSON_COMPANY_ENTRY_TYPE);
    	    	
    	    	logger.info("调用a02查询入职为公司的人员和组织信息selectPersonAndOrgInfo方法开始执行，传入参数：{}", paramMap);
    	    	perAndOrgAndCompanyResultMap = webClientBuilder
                        .build()
                        .post()
                        .uri("/a02permission/persons/personandorginfo")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .body(BodyInserters.fromObject(paramMap))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + SecurityUserUtil.getCurrentToken())
                        .retrieve().bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                        }).block();
    	    	logger.info("调用a02查询入职为公司的人员和组织信息selectPersonAndOrgInfo方法结束，返回数据:{}", perAndOrgAndCompanyResultMap);
    	    	
    	    	if (!"SM-101".equals(perAndOrgAndCompanyResultMap.get(ConstantsUtil.MSG))) {
    	    		resultMap.putAll(perAndOrgAndCompanyResultMap);
    	    		logger.info("工资和人员信息的组合查询selectSalaryAndPersonInfo方法结束，返回数据:{}", resultMap);
    	    		return resultMap;
    	    	}
	    	}
	    	
	    	// 判断入职类型进行数据的合并
	    	// 如果传入参数入职类型为null
	    	if (StringUtils.isEmpty(entryType)) {
	    		// 入职公司的需要和扩展信息合并  perAndOrgAndCompanyResultMap c04PersonExpResultMap
	    		Map<String, Object> perAndOrgAndCompanyAndExpMap = MergeUtil.getMergeResultNoPagination
	    				(perAndOrgAndCompanyResultMap, c04PersonExpResultMap, "id", "personId");
    	    	logger.info("perAndOrgAndCompanyAndExpMap:{}", perAndOrgAndCompanyAndExpMap);
    	    	
	    		// 合并第一次查询的人员信息（返回结果不带有公司）和 合作院校合并 personAndOrgResultMap b01InstitutionResultMap
	    		Map<String, Object> personAndOrgAndInsMap = MergeUtil.getMergeResultNoPagination
	    				(personAndOrgResultMap, b01InstitutionResultMap, "companyOrSchool", "insId");
	    		logger.info("personAndOrgAndInsMap:{}", personAndOrgAndInsMap);
	    		
	    		// 入职学校的需要和扩展信息合并 c04PersonExpResultMap
	    		Map<String, Object> personAndOrgAndInsAndExpMap = MergeUtil.getMergeResultNoPagination
	    				(personAndOrgAndInsMap, c04PersonExpResultMap, "id", "personId");
	    		logger.info("personAndOrgAndInsAndExpMap:{}", personAndOrgAndInsAndExpMap);
	    		
	    		// 得到的两个人员信息加扩展信息均需要和工资合并，然后两个工资合并后的map putAll
	    		// 人员、组织、学院、扩展信息数据集合
	    		@SuppressWarnings("unchecked")
				List<Map<String, Object>> perAndOrgAndInsAndExpList = 
						(List<Map<String, Object>>) personAndOrgAndInsMap.get(ConstantsUtil.RESULTDATA);
	    		if (perAndOrgAndInsAndExpList != null && !perAndOrgAndInsAndExpList.isEmpty()) {
	    			// 人员、组织、合作院校、扩展信息、工资
	    			Map<String, Object> perAndOrgAndInsAndExpAndSalMap = MergeUtil.getMergeResultLeftPagination
	    					(salaryResultMap, personAndOrgAndInsAndExpMap, "userCode", "code");
	    			logger.info("perAndOrgAndInsAndExpAndSalMap:{}", perAndOrgAndInsAndExpAndSalMap);
	    			salaryAndPersonResultMap.putAll(perAndOrgAndInsAndExpAndSalMap);
	    		}
	    		// 人员、组织、公司、扩展信息数据集合
	    		@SuppressWarnings("unchecked")
				List<Map<String, Object>> perAndOrgAndCompanyAndExpList = 
						(List<Map<String, Object>>) perAndOrgAndCompanyAndExpMap.get(ConstantsUtil.RESULTDATA);
	    		if (perAndOrgAndCompanyAndExpList != null && !perAndOrgAndCompanyAndExpList.isEmpty()) {
	    			// 人员、组织、公司、扩展信息、工资
	    			Map<String, Object> perAndOrgAndComAndExpAndSalMap = MergeUtil.getMergeResultLeftPagination
	    					(salaryResultMap, perAndOrgAndCompanyAndExpMap, "userCode", "code");
	    			logger.info("perAndOrgAndComAndExpAndSalMap:{}", perAndOrgAndComAndExpAndSalMap);
	    			salaryAndPersonResultMap.putAll(perAndOrgAndComAndExpAndSalMap);
	    		}
	    		
    	    	
	    	}
	    	// 传入参数入职类型为公司的，即MD2201
	    	if (ConstantsUtil.PERSON_COMPANY_ENTRY_TYPE.equals(entryType)) {
	    		// 合并第一次查询的人员信息（返回结果带有公司）和扩展信息personAndOrgResultMap c04PersonExpResultMap
	    		Map<String, Object> perAndOrgAndCompanyAndExpMap = MergeUtil.getMergeResultNoPagination
	    				(personAndOrgResultMap, c04PersonExpResultMap, "id", "personId");
	    		logger.info("perAndOrgAndCompanyAndExpMap:{}", perAndOrgAndCompanyAndExpMap);
	    		
	    		// 合并人员、组织、公司、扩展信息和工资信息
	    		salaryAndPersonResultMap = MergeUtil.getMergeResultLeftPagination
	    				(salaryResultMap, perAndOrgAndCompanyAndExpMap, "userCode", "code");
	    		
	    	}
	    	// 传入参数入职类型为学院的，即MD2202
	    	if (ConstantsUtil.PERSON_SCHOOL_ENTRY_TYPE.equals(entryType)) {
	    		// 合并第一次查询的人员信息（返回结果不带有公司）和 合作院校合并
	    		Map<String, Object> perAndOrgAndInsMap = MergeUtil.getMergeResultNoPagination
	    				(personAndOrgResultMap, b01InstitutionResultMap, "companyOrSchool", "insId");
	    		logger.info("perAndOrgAndInsMap:{}", perAndOrgAndInsMap);
	    		
	    		// 合并人员、组织、学院和扩展信息
	    		Map<String, Object> personAndOrgAndInsAndExpMap = MergeUtil.getMergeResultNoPagination
	    				(perAndOrgAndInsMap, c04PersonExpResultMap, "id", "personId");
	    		logger.info("personAndOrgAndInsAndExpMap:{}", personAndOrgAndInsAndExpMap);
	    		
	    		// 合并人员、组织、学院、扩展信息和工资信息
	    		salaryAndPersonResultMap = MergeUtil.getMergeResultLeftPagination
	    				(salaryResultMap, personAndOrgAndInsAndExpMap, "userCode", "code");
	    	}
	    	// 最终的人员信息和工资信息集合
	    	logger.info("salaryAndPersonResultMap:{}", salaryAndPersonResultMap);
	    	
	    	// 将合并后的带有分页的数据添加到resultMap
	    	resultMap.putAll(salaryAndPersonResultMap);
	    	
	    	// 处理合并后的数据求和
	    	Map<String, Object> salarySumMap = new HashMap<>();
	    	
	    	// 查询全部flag
			String isLast = String.valueOf(paramMap.get("isLast"));
			
			if ("1".equals(isLast)) {
				paramMap.remove(ConstantsUtil.PAGEINDEX);
				paramMap.remove(ConstantsUtil.PAGESIZE);
				logger.info("调用b07动态条件查询工资selectSalary方法开始执行，入参:{}", paramMap);
	        	Map<String, Object> salaryAll = webClientBuilder
	                    .build()
	                    .post()
	                    .uri("/b07humanresource/salarys/conditions")
	                    .contentType(MediaType.APPLICATION_JSON_UTF8)
	                    .body(BodyInserters.fromObject(paramMap))
	                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + SecurityUserUtil.getCurrentToken())
	                    .retrieve().bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
	                    }).block();
	        	// 计算总计
	        	salarySumMap = b07Service.dealSalarySum(salaryAll);
			}else{
				// 计算小计
				salarySumMap = b07Service.dealSalarySum(salaryResultMap);
			}
	    	
	    	// 取resultMap中的resultData对应的map
	    	@SuppressWarnings("unchecked")
			Map<String, Object> resultDataMap = (Map<String, Object>) resultMap.get(ConstantsUtil.RESULTDATA);
	    	resultDataMap.put("salarySumMap", salarySumMap);
			
		} catch (Exception e) {
			resultMap.put(ConstantsUtil.RESULTDATA, null);
			resultMap.put(ConstantsUtil.STATUS, ConstantsUtil.FAIL);
			resultMap.put(ConstantsUtil.MSG, "EM-101");
			logger.error("工资和人员信息的组合查询selectSalaryAndPersonInfo方法执行异常，错误信息为： {}", e.getMessage());
		}
		logger.info("工资和人员信息的组合查询selectSalaryAndPersonInfo方法结束，返回数据:{}", resultMap);
		return resultMap;
    }
    
    /**
     * 
     * <b>Description:</b><br> 导出工资
     * @param 	paramMap
	 * 				userCodeList:用户编码集合,
	 * 				salalyDate:工资年月,
	 * 				personName:用户名称,
	 *				entryType:入职类型,
	 * 				companyOrSchool:所属机构/院校Id,
	 * 				orgId:组织部门id,
	 * 				personCodeList:用户编码集合,
	 * 				personIdList:用户id集合
	 * 				fileName:导出文件名称
     * @param response
     * 				Http响应
     * @Note
     * <b>Author:</b> 李晓旭
     * <br><b>Date:</b> 2019年10月21日 下午3:34:37
     * <br><b>Version:</b> 1.0
     */
    @PostMapping(value="/exportsalary")
	public void exportSalary(@RequestBody Map<String, Object> paramMap, HttpServletResponse response, HttpServletRequest request){
    	logger.info("导出工资exportSalary方法开始执行，入参:{}", paramMap);
		Map<String, Object> resultMap = new HashMap<>();
		try {
			// 没有最后一页的条件，否则计算合计会出错
			if (paramMap.containsKey("isLast")) {
				paramMap.remove("isLast");
			}
			// 导出文件名称
			String fileName = String.valueOf(paramMap.get("fileName"));
			// 如果文件名为空，则赋默认值为"工资表"
			if (StringUtils.isEmpty(fileName)) {
				fileName = ConstantsUtil.DEFAULT_EXPORT_SALARY_FILE_NAME;
			}
			// 调用查询工资和人员信息的Controller
			resultMap = selectSalaryAndPersonInfo(paramMap,request);
			// 调用Service导出
			b07Service.exportSalary(resultMap, fileName, response);
			resultMap.put(ConstantsUtil.RESULTDATA, null);
			resultMap.put(ConstantsUtil.STATUS, ConstantsUtil.SUCCESS);
			resultMap.put(ConstantsUtil.MSG, "SM-101");
		} catch (Exception e) {
			resultMap.put(ConstantsUtil.RESULTDATA, null);
			resultMap.put(ConstantsUtil.STATUS, ConstantsUtil.FAIL);
			resultMap.put(ConstantsUtil.MSG, "EM-101");
			logger.error("导出工资exportSalary方法异常:{}", e.getMessage());
		}
		logger.info("导出工资exportSalary方法结束，返回数据:{}", resultMap);
    }
}
  
