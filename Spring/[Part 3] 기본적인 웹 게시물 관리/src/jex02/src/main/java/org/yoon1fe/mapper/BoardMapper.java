package org.yoon1fe.mapper;

import java.util.List;

import org.yoon1fe.domain.BoardVO;
import org.yoon1fe.domain.Criteria;

public interface BoardMapper {
	// src/main/resources에 폴더를 만들어서 xml로 쿼리문을 처리했음
	//@Select("select * from tbl_board where bno > 0")
	public List<BoardVO> getList();
	
	public List<BoardVO> getListWithPaging(Criteria cri); 

	public void insert(BoardVO board);
	
	public void insertSelectKey(BoardVO board);
	
	public BoardVO read(Long bno);
	
	public int delete(Long bno);
	
	public int update(BoardVO board);
	
}
