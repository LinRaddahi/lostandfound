package com.taa.lostandfound;

import com.taa.lostandfound.entity.RoleEntity;
import com.taa.lostandfound.entity.UserEntity;
import com.taa.lostandfound.model.LostItemDTO;
import com.taa.lostandfound.model.RegistrationDTO;
import com.taa.lostandfound.model.RoleDTO;
import com.taa.lostandfound.model.UserDTO;
import com.taa.lostandfound.repository.ClaimRepository;
import com.taa.lostandfound.repository.LostItemRepository;
import com.taa.lostandfound.repository.RoleRepository;
import com.taa.lostandfound.repository.UserRepository;
import com.taa.lostandfound.security.JwtUtil;
import com.taa.lostandfound.service.LostItemsService;
import com.taa.lostandfound.service.UserService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import({TestcontainersConfiguration.class, SecurityConfigPropertiesTestConfig.class})
public class LostAndFoundIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserService userService;
    @Autowired
    private LostItemsService lostItemsService;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private LostItemRepository lostItemRepository;
    @Autowired
    private ClaimRepository claimRepository;
    @Autowired
    private EntityManagerFactory emf;

    private String token;
    private String adminToken;

    @BeforeEach
    void setUp() {
        userService.createUser(new RegistrationDTO(TestConstants.USER_ID, TestConstants.PASSWORD, TestConstants.NAME));
        token = jwtUtil.generateToken(
                TestConstants.USER_ID,
                TestConstants.NAME,
                Collections.singletonList(TestConstants.ROLE_USER)
        );

        UserEntity admin = new UserEntity(TestConstants.ADMIN_ID, TestConstants.PASSWORD, TestConstants.ADMIN_NAME);
        Optional<RoleEntity> adminRole = roleRepository.findByName(TestConstants.ROLE_ADMIN);
        admin.setRoles(Collections.singleton(adminRole.orElseGet(() ->
                        roleRepository.save(new RoleEntity(TestConstants.ROLE_ADMIN)))
                )
        );
        userRepository.save(admin);
        adminToken = jwtUtil.generateToken(
                TestConstants.ADMIN_ID,
                TestConstants.ADMIN_NAME,
                Collections.singletonList(TestConstants.ROLE_ADMIN)
        );
    }

    @AfterEach
    void tearDown() {
        claimRepository.deleteAll();
        userRepository.deleteAll();
        lostItemRepository.deleteAll();
        roleRepository.deleteAll();
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        em.createNativeQuery("ALTER TABLE lost_items ALTER COLUMN id RESTART WITH 1").executeUpdate();
        tx.commit();
        em.close();
    }

    @Test
    void authenticate_withValidCredentials_shouldReturnJwtToken() throws Exception {
        mockMvc.perform(get("/authenticate")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("username", TestConstants.USER_ID)
                        .param("password", TestConstants.PASSWORD))
                .andExpect(status().isOk())
                .andExpect(content().string("Login successful"));
    }

    @Test
    void authenticate_withInvalidCredentials_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/authenticate")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", TestConstants.USER_ID)
                        .param("password", "invalidPassword"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticate_withMissingCredentials_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/authenticate")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_withValidData_shouldReturnUserDTO() throws Exception {
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("email", TestConstants.USER_ID)
                        .param("password", TestConstants.PASSWORD)
                        .param("name", TestConstants.NAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TestConstants.USER_ID))
                .andExpect(jsonPath("$.name").value(TestConstants.NAME))
                .andExpect(jsonPath("$.roles[0].name").value(TestConstants.ROLE_USER));
    }

    @Test
    void createUser_withMissingData_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUser_withValidToken_shouldReturnUserDTO() throws Exception {
        mockMvc.perform(get("/get-user")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TestConstants.USER_ID));
    }

    @Test
    void getUser_withInvalidToken_shouldRedirect() throws Exception {
        mockMvc.perform(get("/get-user")
                        .header("Authorization", "Bearer invalidToken"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/login.html"));
    }

    @Test
    void getLostItems_withValidPageAndPerPage_shouldReturnLostItems() throws Exception {
        lostItemsService.createLostItems(new ArrayList<>(
                List.of(new LostItemDTO(1L, TestConstants.ITEM_NAME, 1, TestConstants.PLACE))
        ));

        mockMvc.perform(get("/lost-items")
                        .param("page", "1")
                        .param("perPage", "10")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].itemName").value(TestConstants.ITEM_NAME))
                .andExpect(jsonPath("$.content[0].quantity").value(1))
                .andExpect(jsonPath("$.content[0].place").value(TestConstants.PLACE));
    }

    @Test
    void getLostItems_withInvalidPageOrPerPage_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/lost-items")
                        .param("page", "invalid")
                        .param("perPage", "10")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addLostItems_withValidFile_shouldReturnLostItems() throws Exception {
        mockMvc.perform(multipart("/lost-items")
                        .file(new MockMultipartFile(
                                "lostItemsFile",
                                "LostItems.pdf",
                                MediaType.APPLICATION_PDF_VALUE,
                                new FileInputStream("src/test/resources/LostItems.pdf"))
                        )
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void addLostItems_withInvalidFile_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(multipart("/lost-items")
                        .file(new MockMultipartFile(
                                "lostItemsFile",
                                "LostItems.txt",
                                MediaType.TEXT_PLAIN_VALUE,
                                new FileInputStream("src/test/resources/LostItems.txt"))
                        )
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addLostItems_withoutAdminRole_shouldReturnForbidden() throws Exception {
        mockMvc.perform(multipart("/lost-items")
                        .file(new MockMultipartFile(
                                "lostItemsFile",
                                "LostItems.pdf",
                                MediaType.APPLICATION_PDF_VALUE,
                                new FileInputStream("src/test/resources/LostItems.pdf"))
                        )
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void getLostItemClaims_withValidPageAndPerPage_shouldReturnLostItemClaims() throws Exception {
        lostItemsService.createLostItems(new ArrayList<>(
                List.of(new LostItemDTO(1L, TestConstants.ITEM_NAME, 1, TestConstants.PLACE)))
        );
        lostItemsService.claimItem(
                new UserDTO(TestConstants.USER_ID, TestConstants.NAME, List.of(new RoleDTO("ROLE_USER"))),
                1L,
                1
        );

        mockMvc.perform(get("/lost-items/claims")
                        .param("page", "1")
                        .param("perPage", "10")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].quantity").value(1));
    }

    @Test
    void getLostItemClaims_withInvalidPageOrPerPage_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/lost-items/claims")
                        .param("page", "invalid")
                        .param("perPage", "10")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getLostItemClaims_withoutAdminRole_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/lost-items/claims")
                        .param("page", "1")
                        .param("perPage", "10")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void claimLostItem_withValidItemIdAndQuantity_shouldReturnClaimDTO() throws Exception {
        lostItemsService.createLostItems(new ArrayList<>(
                List.of(new LostItemDTO(1L, TestConstants.ITEM_NAME, 1, TestConstants.PLACE)))
        );
        
        mockMvc.perform(post("/lost-items/1/claim")
                        .param("quantity", "1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(1));
    }

    @Test
    void claimLostItem_withInvalidItemId_shouldReturnNotFound() throws Exception {
        mockMvc.perform(post("/lost-items/2/claim")
                        .param("quantity", "1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

}
