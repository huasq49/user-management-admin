package com.usermonitor.server.controller;
import com.usermonitor.server.ApiResponse;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
@RestControllerAdvice
public class ExceptionControllerAdvice {
 @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
 ResponseEntity<ApiResponse<Void>> notFound(org.springframework.web.servlet.resource.NoResourceFoundException e){
  return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail(404,"接口不存在，请检查服务器地址或更新服务端"));
 }
 @ExceptionHandler(IllegalArgumentException.class) ResponseEntity<ApiResponse<Void>> bad(IllegalArgumentException e){return ResponseEntity.badRequest().body(ApiResponse.fail(400,e.getMessage()));}
 @ExceptionHandler(SecurityException.class) ResponseEntity<ApiResponse<Void>> unauthorized(SecurityException e){return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.fail(401,e.getMessage()));}
 @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<ApiResponse<Void>> validation(MethodArgumentNotValidException e){var f=e.getBindingResult().getFieldErrors().get(0);String message=switch(f.getField()){case "donateUrl"->"赞助地址格式不正确";case "downloadUrl"->"下载地址不能为空";case "title"->"标题不能为空";case "content"->"内容不能为空";default->"提交内容不完整："+f.getField();};return ResponseEntity.badRequest().body(ApiResponse.fail(400,message));}
 @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class) ResponseEntity<ApiResponse<Void>> tooLarge(){return ResponseEntity.status(413).body(ApiResponse.fail(413,"上传文件过大"));}
 @ExceptionHandler({org.springframework.http.converter.HttpMessageNotReadableException.class, org.springframework.web.bind.MissingServletRequestParameterException.class, org.springframework.web.bind.MissingRequestHeaderException.class, org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class}) ResponseEntity<ApiResponse<Void>> malformed(){return ResponseEntity.badRequest().body(ApiResponse.fail(400,"请求格式或参数无效"));}
 @ExceptionHandler(Exception.class) ResponseEntity<ApiResponse<Void>> error(Exception e){org.slf4j.LoggerFactory.getLogger(getClass()).error("Unhandled server exception: {}",e.getClass().getName());return ResponseEntity.status(500).body(ApiResponse.fail(500,"服务器内部错误"));}
}