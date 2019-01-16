package joowon.study.restapi.config;

import joowon.study.restapi.accounts.Account;
import joowon.study.restapi.accounts.AccountRole;
import joowon.study.restapi.accounts.AccountService;
import joowon.study.restapi.common.BaseControllerTest;
import joowon.study.restapi.common.TestDescription;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

import static org.junit.Assert.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthServerConfigTest extends BaseControllerTest {

    @Autowired
    AccountService accountService;

    @Test
    @TestDescription("인증 토큰을 발급 받는 테스트")
    public void getAuthToken() throws Exception {
        // Given
        String username = "admin@email.com";
        String password = "admin";
        Account admin = Account.builder()
                .email(username)
                .password(password)
                .roles(Set.of(AccountRole.ADMIN, AccountRole.USER))
                .build();
        this.accountService.saveAccount(admin);

        String clientId = "myApp";
        String cliendSecret = "pass";

        // When & Then
        this.mockMvc.perform(post("/oauth/token")
                    .with(httpBasic(clientId, cliendSecret))
                    .param("username", username)
                    .param("password", password)
                    .param("grant_type","password"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("access_token").exists());

    }
}