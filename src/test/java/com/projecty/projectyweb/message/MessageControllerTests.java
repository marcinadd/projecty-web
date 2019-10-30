package com.projecty.projectyweb.message;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Optional;

import javax.sql.rowset.serial.SerialBlob;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.projecty.projectyweb.ProjectyWebApplication;
import com.projecty.projectyweb.message.association.Association;
import com.projecty.projectyweb.message.association.AssociationRepository;
import com.projecty.projectyweb.message.attachment.Attachment;
import com.projecty.projectyweb.user.User;
import com.projecty.projectyweb.user.UserRepository;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ProjectyWebApplication.class)
@AutoConfigureMockMvc
public class MessageControllerTests {
	@MockBean(name = "userRepository")
	UserRepository userRepository;

	@MockBean(name = "messageRepository")
	MessageRepository messageRepository;

	@MockBean(name = "associationRepository")
	AssociationRepository associationRepository;

	@Autowired
	private MockMvc mockMvc;

	private Message message;
	private Message replyMessage;
	private String recipientUsername;

	private Association associationForRecipient;
	private Association associationForSender;

	@Before
	public void init() throws SQLException {
		User user = new User();
		user.setId(1L);
		user.setUsername("user");
		user.setEmail("user@example.com");

		User user1 = new User();
		user1.setId(2L);
		user1.setUsername("user1");
		user1.setEmail("user1@example.com");

		User user2 = new User();
		user2.setId(3L);
		user2.setUsername("user2");

		message = new Message();
		message.setId(1L);
		message.setText("This is sample message");
		message.setTitle("sample title");
		recipientUsername = "user1";
		message.setRecipient(user);
		message.setSender(user1);

		associationForRecipient = new Association();
		associationForRecipient.setUser(user);
		associationForRecipient.setMessage(message);
		associationForRecipient.setId(1L);

		associationForSender = new Association();
		associationForSender.setUser(user1);
		associationForSender.setMessage(message);
		associationForSender.setId(2L);

		byte[] bytes = new byte[] { 0, 1, 2, 3, 4, 5 };
		Attachment attachment = new Attachment();
		attachment.setFile(new SerialBlob(bytes));
		message.setAttachments(Collections.singletonList(attachment));

		replyMessage = new Message();
		replyMessage.setId(11L);
		replyMessage.setText("This is sample reply message");
		replyMessage.setTitle("sample reply title");

		Mockito.when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
		Mockito.when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
		Mockito.when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
		Mockito.when(userRepository.findByUsername(user1.getUsername())).thenReturn(Optional.of(user1));
		Mockito.when(userRepository.findByUsername(user2.getUsername())).thenReturn(Optional.of(user2));
		Mockito.when(messageRepository.findById(message.getId())).thenReturn(Optional.ofNullable(message));
		Mockito.when(messageRepository.save(eq(message))).thenReturn(message);
		Mockito.when(messageRepository.findById(replyMessage.getId())).thenReturn(Optional.ofNullable(replyMessage));

		Mockito.when(associationRepository.findFirstByUserAndMessage(Mockito.eq(user1), any(Message.class)))
				.thenReturn(Optional.ofNullable(associationForSender));
		Mockito.when(associationRepository.findFirstByUserAndMessage(Mockito.eq(user2), any(Message.class)))
				.thenReturn(Optional.empty());
		Mockito.when(associationRepository.findFirstByUserAndMessage(Mockito.eq(user), any(Message.class)))
				.thenReturn(Optional.ofNullable(associationForRecipient));
	}

	@Test
	@WithMockUser
	public void givenRequestOnSendMessageToUserWhichNotExists_shouldReturnBadRequest() throws Exception {
		Message message = new Message();
		message.setId(2L);
		message.setText("This is sample message");
		message.setTitle("sample title");
		User user = new User();
		User user1 = new User();
		message.setRecipient(user);
		message.setSender(user1);
		mockMvc.perform(post("/message/sendMessage/to/".concat("notExistsUsername")).flashAttr("message", message))
				.andExpect(status().isBadRequest());
	}

	@Test
	@WithMockUser
	public void givenRequestOnSendMessageToYourself_shouldReturnBadRequest() throws Exception {
		Message message = new Message();
		message.setId(3L);
		message.setText("This is sample message");
		message.setTitle("sample title");
		User user = new User();
		User user1 = new User();
		message.setRecipient(user);
		message.setSender(user1);
		mockMvc.perform(post("/message/sendMessage/to/".concat("user")).flashAttr("message", message))
				.andExpect(status().isBadRequest());
	}

	@Test
	@WithMockUser
	public void givenRequestOnSendMessage_shouldReturnOk() throws Exception {
		mockMvc.perform(post("/message/sendMessage/to/".concat(recipientUsername)).flashAttr("message", message))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser
	public void givenRequestOnViewMessage_shouldReturnMessage() throws Exception {
		mockMvc.perform(get("/message/viewMessage/message/1")).andExpect(status().isOk())
				.andExpect(jsonPath("$.text").value(message.getText()));
	}

	@Test
	@WithMockUser
	public void givenRequestOnViewMessageWhichNotFound_shouldReturnNotFound() throws Exception {
		mockMvc.perform(get("/message/viewMessage/message/2")).andExpect(status().isNotFound());
	}

	@Test
	@WithMockUser
	public void givenRequestOnReceivedMessages_shouldReturnReceivedMessages() throws Exception {
		mockMvc.perform(get("/message/receivedMessages")).andExpect(status().isOk()).andExpect(jsonPath("$").isArray());
	}

	@Test
	@WithMockUser
	public void givenRequestOnSentMessages_shouldReturnSentMessages() throws Exception {
		mockMvc.perform(get("/message/sentMessages")).andExpect(status().isOk()).andExpect(jsonPath("$").isArray());
	}

	@Test
	@WithMockUser
	public void givenRequestOnGetUnreadMessageCount_shouldReturnNumber() throws Exception {
		mockMvc.perform(get("/message/getUnreadMessageCount")).andExpect(jsonPath("$").isNumber());
	}

	@Test
	@WithMockUser
	public void givenRequestOnDownloadFile_shouldReturnFileToDownload() throws Exception {
		mockMvc.perform(get("/message/downloadFile/message/1")).andExpect(status().isOk());
	}

	@Test
	@WithMockUser("user2")
	public void givenRequestOnDownloadFileWithNoPermission_shouldReturnFileNotFound() throws Exception {
		mockMvc.perform(get("/message/downloadFile/message/1")).andExpect(status().isNotFound());
	}

	@Test
	@WithMockUser("user2")
	public void givenRequestOnMessageWithNoPermission_shouldReturnNotFound() throws Exception {
		mockMvc.perform(get("/message/viewMessage/message/1")).andExpect(status().isNotFound());
	}

	@Test
	@WithMockUser
	public void givenRequestOnReplyToMessageWhichNotExists_shouldReturnNotFound() throws Exception {
		Message replyMessage = new Message();
		replyMessage.setId(11L);
		replyMessage.setText("This is sample reply message");
		replyMessage.setTitle("sample reply title");
		mockMvc.perform(post("/message/12/reply").flashAttr("message", replyMessage)).andExpect(status().isNotFound());
	}

	@Test
	@WithMockUser("user1")
	public void givenRequestOnReplyToYourself_shouldReturnBadRequest() throws Exception {
		Message replyMessage = new Message();
		replyMessage.setId(11L);
		replyMessage.setText("This is sample reply message");
		replyMessage.setTitle("sample reply title");
		mockMvc.perform(post("/message/1/reply").flashAttr("message", replyMessage)).andExpect(status().isBadRequest());
	}

	@Test
	@WithMockUser
	public void givenRequestOnReply_shouldReturnOk() throws Exception {
		Message replyMessage = new Message();
		replyMessage.setId(11L);
		replyMessage.setText("This is sample reply message");
		replyMessage.setTitle("sample reply title");
		mockMvc.perform(post("/message/1/reply").flashAttr("message", replyMessage)).andExpect(status().isOk());
	}

}
