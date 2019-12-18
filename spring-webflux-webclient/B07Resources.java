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
 * @version 1.0
 * @Note
 * <b>ProjectName:</b> s01cross
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

     * @return
     * @Note

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
    
}
  
