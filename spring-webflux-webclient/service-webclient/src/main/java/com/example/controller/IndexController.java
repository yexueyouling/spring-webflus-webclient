package com.example.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class IndexController {

	/**
	 * 1、webClient是异步通信
	 * 2、获取响应有两种方式：retrieve()和exchange(),exchange包含的信息多，包含了head的信息，本质上是ClientResponse,而retrieve是获取响应body的快捷方式。
	 * 3、可以处理为同步通信，使用block进行阻塞，获取响应结果。
	 * 4、webClient获得响应有两个方法：bodyToMono和bodyToFlux，两者的返回类型分别是Mono<T>和Flux<T>，两者的区别在于 Mono<T> 的响应结果，仅包含 0-1 个结果，而 Flux<T> 可以包含多个结果。
	 * @param args
	 */
	public static void main(String[] args) {
		// 1 创建实例
		WebClient webClient = WebClient.create();
		// get请求没有参数  uri：请求路径 retrieve：获取响应体 bodyToMono响应数据类型转化
		Mono<String> mono = webClient.get().uri("http://localhost:8080/getString").retrieve().bodyToMono(String.class);
		String result = null;
		result = mono.block();
		System.out.println(result);
		
		// 2 get请求有参数 使用占位符的方式发送参数
		WebClient webClient1 = WebClient.create("http://localhost:8080");
		Mono<String> mono1 = webClient1.get().uri("/getStringValue?username=你好，mina&param=cesh").retrieve().bodyToMono(String.class);
		String result1 = null;
		result1 = mono1.block();
		System.out.println(result1);
		
		// 3 url变量发送参数方式 Map方式
		Map<String, Object> map = new HashMap<>();
		map.put("username", "xingmin南师大");
		map.put("age", "16");
		WebClient webClient2 = WebClient.create("http://localhost:8080");
		Mono<String> mono2 = webClient2.get().uri("/getPathVarible/{username}/{age}", map).retrieve().bodyToMono(String.class);
		String result2 = null;
		result2 = mono2.block();
		System.out.println(result2);
		
		// 3url变量发送参数方式 直接拼接数据方式
		WebClient webClient3 = WebClient.create("http://localhost:8080");
		Mono<String> mono3 = webClient3.get().uri("/getPathVarible/woshi一个测试/16").retrieve().bodyToMono(String.class);
		String result3 = null;
		result3 = mono3.block();
		System.out.println(result3);
		
		// 4使用uriBuilder传递参数
		Mono<String> resp = WebClient.create()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("http")
                        .host("localhost")
                        .port("8080")
                        .path("/getStringValue")
                        .queryParam("username", "用户名")
                        .queryParam("param", "test")
                        .build())
                .retrieve()
                .bodyToMono(String.class);
        System.out.println(resp.block());
		
		
		
		// 5 post 发map
        Map<String, Object> postMap = new HashMap<>();
		postMap.put("username", "xingm南师大");
		postMap.put("age", "16");   
	    // 使用post方式发送数据时，需指定contentType
		Mono<String> resPostMap = WebClient.create()
				.post()
				.uri("http://localhost:8080/postMap")
				.contentType(MediaType.APPLICATION_JSON)
				.syncBody(postMap).retrieve().bodyToMono(String.class);
		System.out.println(resPostMap.block());
		
		
		// 6 post 发表单，MultiValueMap创建的对象表示表单，使用post方式发送表单。
		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("username","value1");
        formData.add("age","value2");
		Mono<String> resForm = WebClient.create()
				.post()
				.uri("http://localhost:8080/postForm")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.body(BodyInserters.fromFormData(formData)).retrieve().bodyToMono(String.class);
		System.out.println(resForm.block());
		
		// 7使用bean发post请求
		Book book = new Book();
        book.setName("name");
        book.setTitle("this is title");
        Mono<String> respBean = WebClient.create().post()
                .uri("http://localhost:8080/postBeanJson")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .body(Mono.just(book),Book.class)
                .retrieve().bodyToMono(String.class);
        System.out.println(respBean.block());
		
		// 8 使用json发post请求
		String json = "{\n\"title\" : \"this is title\",\n\"author\" : \"this is author\"\n}";
		Mono<String> respJson = WebClient.create().post()
	            .uri("http://localhost:8080/postBeanJson")
	            .contentType(MediaType.APPLICATION_JSON_UTF8)
	            .body(BodyInserters.fromObject(json))
	            .retrieve().bodyToMono(String.class);
	    System.out.println(respJson.block());
		
		// 9上传文件
		HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        HttpEntity<ClassPathResource> entity = new HttpEntity<>(new ClassPathResource("image.jpg"), headers);
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("file", entity);
        Mono<String> respUpload = WebClient.create().post()
                .uri("http://localhost:8080/upload")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(parts))
                .retrieve().bodyToMono(String.class);
       System.out.println(respUpload.block());
		
		
		// 10  下载图片 下载简书的图标到E盘下
		try {
		Mono<Resource> respImg = WebClient.create().get()
	            .uri("https://cdn2.jianshu.io/assets/web/logo-58fd04f6f0de908401aa561cda6a0688.png")
	            .accept(MediaType.IMAGE_PNG)
	            .retrieve().bodyToMono(Resource.class);
	    Resource resource = respImg.block();
	    BufferedImage bufferedImage = ImageIO.read(resource.getInputStream());
		ImageIO.write(bufferedImage, "png", new File("e:/下载的图片captcha.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// 11  delete方法 类似于get 
		WebClient webClientDel = WebClient.create("http://localhost:8080");
		Mono<String> monoDel = webClientDel.delete().uri("/delete?username=a里").retrieve().bodyToMono(String.class);
		String resultDel = null;
		resultDel = monoDel.block();
		System.out.println(resultDel);
		
		// 12 put方法 类似于post
		Map<String, Object> mapPut = new HashMap<>();
		mapPut.put("username", "xingm南师大");
		mapPut.put("age", "16");   
		// 发送put请求 
		Mono<String> respPut = WebClient.create()
				.put()
				.uri("http://localhost:8080/put")
				.contentType(MediaType.APPLICATION_JSON)
				.syncBody(mapPut).retrieve().bodyToMono(String.class);
		System.out.println(respPut.block());
		
		// 13 下载文件
		try {
			Mono<ClientResponse> respDownLoad = WebClient.create().get()
	                .uri("http://localhost:8080/download")
	                .accept(MediaType.APPLICATION_OCTET_STREAM)
	                .exchange(); // retrieve()被换成了exchage(),在那么接收类型就替换为ClientResponse
	        ClientResponse response = respDownLoad.block();
	        String disposition = response.headers().asHttpHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
	        // 指定文件的存储路径，进行文件的下载处理
	        String fileName = disposition.substring(disposition.indexOf("=")+1);
	        Resource resource = response.bodyToMono(Resource.class).block();
	        File out = new File(fileName);
			FileUtils.copyInputStreamToFile(resource.getInputStream(),out);
			System.out.println(out.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// 14 flux接受返回多结果处理List<Entity>
		Flux<Book> respFlux = WebClient.create("http://localhost:8080/postFlux")
				.post()
				.contentType(MediaType.APPLICATION_JSON)
				.syncBody(postMap)
				.retrieve()
				.bodyToFlux(Book.class);
		System.out.println("14flux接受返回多结果处理:" + respFlux.subscribe());
		List<Book> listBook = respFlux.collectList().block();
		System.out.println(listBook.get(0).getName());
		
		// 15 flux接受返回多结果处理List<map>
		Flux<Map> respFluxMap = WebClient.create("http://localhost:8080/postFluxMap")
				.post()
				.retrieve()
				.bodyToFlux(Map.class);
		System.out.println("15fluxMap接受返回多结果处理:" + respFlux.subscribe());
		List<Map> listMap = respFluxMap.collectList().block();
		System.out.println(listMap);
	}
	
	
	static class Book {
        String name;
        String title;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }

}
