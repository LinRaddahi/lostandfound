package com.taa.lostandfound.controller;

import com.taa.lostandfound.TestConstants;
import com.taa.lostandfound.model.ClaimDTO;
import com.taa.lostandfound.model.LostItemDTO;
import com.taa.lostandfound.model.UserDTO;
import com.taa.lostandfound.security.JwtUtil;
import com.taa.lostandfound.service.LostItemsService;
import com.taa.lostandfound.service.PDFService;
import com.taa.lostandfound.service.ParserService;
import com.taa.lostandfound.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;
import org.testcontainers.utility.TestcontainersConfiguration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LostItemsController.class)
@Import({TestcontainersConfiguration.class})
@AutoConfigureMockMvc
public class LostItemsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LostItemsService lostItemsService;

    @MockBean
    private PDFService pdfService;

    @MockBean
    private ParserService parserService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserDetails userDetails;

    @MockBean
    private UserService userService;

    @BeforeEach
    public void setUp() {
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(TestConstants.ROLE_USER));
        doReturn(authorities).when(userDetails).getAuthorities();
        when(jwtUtil.extractUserId(anyString())).thenReturn(TestConstants.USER_ID);
        when(userService.loadUserByUsername(anyString())).thenReturn(userDetails);
        when(jwtUtil.validateToken(anyString(), any(UserDetails.class))).thenReturn(true);
    }

    @Test
    public void testGetLostItems() throws Exception {
        Page<LostItemDTO> lostItemsPage = new PageImpl<>(
                List.of(new LostItemDTO(TestConstants.ITEM_NAME, TestConstants.QUANTITY, TestConstants.PLACE))
        );
        when(lostItemsService.getLostItems(anyInt(), anyInt())).thenReturn(lostItemsPage);

        mockMvc.perform(get("/lost-items")
                        .param("page", "1")
                        .param("perPage", "10")
                        .header("Authorization", "Bearer validToken")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].itemName").value(TestConstants.ITEM_NAME));
    }

    @Test
    public void testAddLostItems() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "lostItemsFile",
                "test.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "PDF content".getBytes()
        );
        ArrayList<LostItemDTO> lostItems = new ArrayList<>();
        lostItems.add(new LostItemDTO(TestConstants.ITEM_NAME, TestConstants.QUANTITY, TestConstants.PLACE));
        when(pdfService.extractPDFContent(any(MultipartFile.class))).thenReturn("PDF content");
        when(parserService.parseLostItems(anyString())).thenReturn(lostItems);
        when(lostItemsService.createLostItems(any())).thenReturn(lostItems);
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(TestConstants.ROLE_ADMIN));
        doReturn(authorities).when(userDetails).getAuthorities();

        mockMvc.perform(multipart("/lost-items")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .header("Authorization", "Bearer validToken")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].itemName").value(TestConstants.ITEM_NAME));
    }

    @Test
    public void testGetLostItemClaims() throws Exception {
        Page<ClaimDTO> claimsPage = new PageImpl<>(
                List.of(new ClaimDTO(
                                1L,
                                new UserDTO(TestConstants.USER_ID, TestConstants.NAME, new ArrayList<>()),
                                new LostItemDTO(TestConstants.ITEM_NAME, TestConstants.QUANTITY, TestConstants.PLACE),
                                TestConstants.QUANTITY
                        )
                ));
        when(lostItemsService.getLostItemClaims(anyInt(), anyInt())).thenReturn(claimsPage);
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        doReturn(authorities).when(userDetails).getAuthorities();

        mockMvc.perform(get("/lost-items/claims")
                        .param("page", "1")
                        .param("perPage", "10")
                        .header("Authorization", "Bearer validToken")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].user.name").value(TestConstants.NAME))
                .andExpect(jsonPath("$.content[0].lostItem.itemName").value(TestConstants.ITEM_NAME));

    }

    @Test
    public void testClaimLostItem() throws Exception {
        String token = "Bearer validToken";
        UserDTO userDTO = new UserDTO(TestConstants.USER_ID, TestConstants.NAME, new ArrayList<>());
        ClaimDTO claimDTO = new ClaimDTO(
                1L,
                new UserDTO(TestConstants.USER_ID, TestConstants.NAME, new ArrayList<>()),
                new LostItemDTO(TestConstants.ITEM_NAME, TestConstants.QUANTITY, TestConstants.PLACE),
                TestConstants.QUANTITY
        );
        when(jwtUtil.convert(anyString())).thenReturn(userDTO);
        when(lostItemsService.claimItem(any(UserDTO.class), anyLong(), anyInt())).thenReturn(claimDTO);

        mockMvc.perform(post("/lost-items/1/claim")
                        .header("Authorization", token)
                        .param("quantity", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lostItem.itemName").value(TestConstants.ITEM_NAME))
                .andExpect(jsonPath("$.user.name").value(TestConstants.NAME));
    }
}