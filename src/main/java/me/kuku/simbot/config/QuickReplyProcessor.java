package me.kuku.simbot.config;

import catcode.StringTemplate;
import love.forte.simbot.api.message.MessageContent;
import love.forte.simbot.api.message.MessageContentBuilder;
import love.forte.simbot.api.message.MessageContentBuilderFactory;
import love.forte.simbot.api.message.containers.GroupContainer;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.message.events.MessageGet;
import love.forte.simbot.api.message.events.MsgGet;
import love.forte.simbot.api.message.events.PrivateMsg;
import love.forte.simbot.api.sender.MsgSender;
import love.forte.simbot.api.sender.Sender;
import love.forte.simbot.listener.ListenerFunctionInvokeData;
import love.forte.simbot.processor.ListenResultProcessor;
import love.forte.simbot.processor.ListenResultProcessorContext;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@SuppressWarnings("DuplicatedCode")
@Service
public class QuickReplyProcessor implements ListenResultProcessor {

	@Autowired
	private MessageContentBuilderFactory messageContentBuilderFactory;
	@Autowired
	private StringTemplate stringTemplate;

	@Override
	public int getPriority() {
		return ListenResultProcessor.super.getPriority();
	}

	@NotNull
	@Override
	public Boolean processor(@NotNull ListenResultProcessorContext processContext) {
		ListenerFunctionInvokeData listenerFunctionInvokeData = processContext.getListenerFunctionInvokeData();

		MsgSender msgSender = listenerFunctionInvokeData.getMsgSender();
		MsgGet msgGet = listenerFunctionInvokeData.getMsgGet();
		Sender sender = msgSender.SENDER;

		Object result = processContext.getListenResult().getResult();
		if (result == null) return false;

		if (msgGet instanceof MessageGet){
			MessageGet messageGet = (MessageGet) msgGet;
			MessageContentBuilder builder = messageContentBuilderFactory.getMessageContentBuilder();
			if (result instanceof String){
				// 发送文字
				String msg = stringTemplate.at(messageGet.getAccountInfo().getAccountCodeNumber()) + "\n" +
						result;
				sendMessage(messageGet, sender, msg);
			}else if (result instanceof byte[]){
				MessageContent messageContent = builder.image((byte[]) result).build();
				sendMessage(messageGet, sender, messageContent);
			}
		}
		return true;
	}

	private void sendMessage(MessageGet messageGet, Sender sender, MessageContent messageContent){
		if (messageGet instanceof PrivateMsg){
			if (messageGet instanceof GroupContainer){
				sender.sendPrivateMsg(messageGet.getAccountInfo(), ((GroupContainer) messageGet).getGroupInfo(),
						messageContent);
			}else {
				sender.sendPrivateMsg(messageGet, messageContent);
			}
		}else if (messageGet instanceof GroupMsg){
			sender.sendGroupMsg(((GroupMsg) messageGet).getGroupInfo(), messageContent);
		}
	}

	private void sendMessage(MessageGet messageGet, Sender sender, String msg){
		if (messageGet instanceof PrivateMsg){
			if (messageGet instanceof GroupContainer){
				sender.sendPrivateMsg(messageGet.getAccountInfo(), ((GroupContainer) messageGet).getGroupInfo(),
						msg);
			}else {
				sender.sendPrivateMsg(messageGet, msg);
			}
		}else if (messageGet instanceof GroupMsg){
			sender.sendGroupMsg(((GroupMsg) messageGet).getGroupInfo(), msg);
		}
	}
}
