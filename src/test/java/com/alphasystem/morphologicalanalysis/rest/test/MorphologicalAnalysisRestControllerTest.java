package com.alphasystem.morphologicalanalysis.rest.test;

import com.alphasystem.morphologicalanalysis.MorphologicalAnalysisApplication;
import com.alphasystem.morphologicalanalysis.util.DataInitializationTool;
import com.alphasystem.morphologicalanalysis.util.Script;
import org.hamcrest.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author sali
 */
@SpringBootTest(classes = MorphologicalAnalysisApplication.class)
@WebAppConfiguration
public class MorphologicalAnalysisRestControllerTest extends AbstractTestNGSpringContextTests {

    private MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));

    private MockMvc mockMvc;
    private HttpMessageConverter mappingJackson2HttpMessageConverter;
    private int totalNumberOfChapters;

    @Value("${script.name:SIMPLE_ENHANCED}") private String scriptName;
    @Autowired private WebApplicationContext webApplicationContext;
    @Autowired private DataInitializationTool dataInitializationTool;
    @Autowired private MongoDbFactory mongoDbFactory;

    @BeforeClass
    public void beforeClass() throws Exception {
        Reporter.log("Running beforeClass", true);
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // setup data
        final Script script = Script.valueOf(scriptName);
        dataInitializationTool.createChapter(1, script);
        totalNumberOfChapters++;
        dataInitializationTool.createChapter(49, script);
        totalNumberOfChapters++;
    }

    @AfterSuite
    public void afterSuite() {
        Reporter.log("Running afterSuite", true);
        mongoDbFactory.getDb().dropDatabase();
    }

    @Autowired
    void setConverters(HttpMessageConverter<?>[] converters) {
        this.mappingJackson2HttpMessageConverter = Arrays.stream(converters)
                .filter(hmc -> hmc instanceof MappingJackson2HttpMessageConverter)
                .findAny()
                .orElse(null);
        Assert.assertNotNull(this.mappingJackson2HttpMessageConverter, "the JSON message converter must not be null");
    }

    @Test
    public void findAllChapters() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/morphological/chapters"))
                .andExpect(content().contentType(contentType))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(totalNumberOfChapters)))
                .andExpect(jsonPath("$[0].id", Matchers.equalTo("chapter:1")))
                .andExpect(jsonPath("$[0].displayName", Matchers.equalTo("chapter:1")));
    }

    @SuppressWarnings("unchecked")
    private String json(Object o) throws IOException {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
        this.mappingJackson2HttpMessageConverter.write(o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
        return mockHttpOutputMessage.getBodyAsString();
    }
}
