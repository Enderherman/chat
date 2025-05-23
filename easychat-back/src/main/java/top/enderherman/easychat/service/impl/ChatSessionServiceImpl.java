package top.enderherman.easychat.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import top.enderherman.easychat.entity.enums.PageSize;
import top.enderherman.easychat.entity.query.ChatSessionQuery;
import top.enderherman.easychat.entity.po.ChatSession;
import top.enderherman.easychat.entity.vo.PaginationResultVO;
import top.enderherman.easychat.entity.query.SimplePage;
import top.enderherman.easychat.mappers.ChatSessionMapper;
import top.enderherman.easychat.service.ChatSessionService;

import top.enderherman.easychat.utils.StringUtils;


/**
 * 会话信息表 业务接口实现
 */
@Service("chatSessionService")
public class ChatSessionServiceImpl implements ChatSessionService {

	@Resource
	private ChatSessionMapper<ChatSession, ChatSessionQuery> chatSessionMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<ChatSession> findListByParam(ChatSessionQuery param) {
		return this.chatSessionMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(ChatSessionQuery param) {
		return this.chatSessionMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<ChatSession> findListByPage(ChatSessionQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<ChatSession> list = this.findListByParam(param);
		PaginationResultVO<ChatSession> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(ChatSession bean) {
		return this.chatSessionMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<ChatSession> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.chatSessionMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<ChatSession> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.chatSessionMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(ChatSession bean, ChatSessionQuery param) {
		StringUtils.checkParam(param);
		return this.chatSessionMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(ChatSessionQuery param) {
		StringUtils.checkParam(param);
		return this.chatSessionMapper.deleteByParam(param);
	}

	/**
	 * 根据SessionId获取对象
	 */
	@Override
	public ChatSession getChatSessionBySessionId(String sessionId) {
		return this.chatSessionMapper.selectBySessionId(sessionId);
	}

	/**
	 * 根据SessionId修改
	 */
	@Override
	public Integer updateChatSessionBySessionId(ChatSession bean, String sessionId) {
		return this.chatSessionMapper.updateBySessionId(bean, sessionId);
	}

	/**
	 * 根据SessionId删除
	 */
	@Override
	public Integer deleteChatSessionBySessionId(String sessionId) {
		return this.chatSessionMapper.deleteBySessionId(sessionId);
	}
}