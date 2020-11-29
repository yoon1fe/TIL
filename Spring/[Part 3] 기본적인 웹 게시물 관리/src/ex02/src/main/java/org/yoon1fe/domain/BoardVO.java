package org.yoon1fe.domain;

import java.util.Date;

import lombok.Data;

// lombok을 이용해서 생성자, getter/setter, toString() 생성을 위한 @Data 어노테이션
@Data
public class BoardVO {
	private Long bno;
	private String title;
	private String content;
	private String writer;
	private Date regdate;
	private Date updateDate;
}
