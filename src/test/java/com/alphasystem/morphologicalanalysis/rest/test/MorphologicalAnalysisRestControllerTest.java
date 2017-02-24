package com.alphasystem.morphologicalanalysis.rest.test;

import com.alphasystem.arabic.model.ArabicLetterType;
import com.alphasystem.arabic.model.ArabicWord;
import com.alphasystem.arabic.model.NamedTemplate;
import com.alphasystem.morphologicalanalysis.MorphologicalAnalysisApplication;
import com.alphasystem.morphologicalanalysis.common.model.VerseTokenPairGroup;
import com.alphasystem.morphologicalanalysis.common.model.VerseTokensPair;
import com.alphasystem.morphologicalanalysis.morphology.model.MorphologicalEntry;
import com.alphasystem.morphologicalanalysis.morphology.model.RootLetters;
import com.alphasystem.morphologicalanalysis.morphology.model.support.VerbalNoun;
import com.alphasystem.morphologicalanalysis.util.DataInitializationTool;
import com.alphasystem.morphologicalanalysis.util.Script;
import com.alphasystem.morphologicalanalysis.wordbyword.model.AbstractProperties;
import com.alphasystem.morphologicalanalysis.wordbyword.model.Location;
import com.alphasystem.morphologicalanalysis.wordbyword.model.NounProperties;
import com.alphasystem.morphologicalanalysis.wordbyword.model.ParticleProperties;
import com.alphasystem.morphologicalanalysis.wordbyword.model.Token;
import com.alphasystem.morphologicalanalysis.wordbyword.model.support.NounPartOfSpeechType;
import com.alphasystem.morphologicalanalysis.wordbyword.model.support.NounStatus;
import com.alphasystem.morphologicalanalysis.wordbyword.model.support.ParticlePartOfSpeechType;
import com.alphasystem.morphologicalanalysis.wordbyword.model.support.WordType;
import com.alphasystem.morphologicalanalysis.wordbyword.repository.TokenRepository;
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
import org.springframework.test.web.servlet.ResultActions;
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
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author sali
 */
@SpringBootTest(classes = MorphologicalAnalysisApplication.class)
@WebAppConfiguration
public class MorphologicalAnalysisRestControllerTest extends AbstractTestNGSpringContextTests {

    private static final int FIRST_CHAPTER_NUMBER = 1;
    private static final int FIRST_VERSE_NUMBER = 1;
    private static final int FIRST_TOKEN_NUMBER = 1;
    private static final String DISPLAY_NAME = String.format("%s:%s:%s", FIRST_CHAPTER_NUMBER, FIRST_VERSE_NUMBER, FIRST_TOKEN_NUMBER);
    private MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));

    private MockMvc mockMvc;
    private HttpMessageConverter mappingJackson2HttpMessageConverter;
    private int totalNumberOfChapters;
    private Token token;

    @Value("${script.name:SIMPLE_ENHANCED}") private String scriptName;
    @Autowired private WebApplicationContext webApplicationContext;
    @Autowired private DataInitializationTool dataInitializationTool;
    @Autowired private MongoDbFactory mongoDbFactory;
    @Autowired private TokenRepository tokenRepository;

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

        token = tokenRepository.findByChapterNumberAndVerseNumberAndTokenNumber(FIRST_CHAPTER_NUMBER, FIRST_VERSE_NUMBER,
                FIRST_TOKEN_NUMBER);
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

    @Test(dependsOnMethods = "findAllChapters")
    public void getTokens() throws Exception {
        VerseTokenPairGroup group = new VerseTokenPairGroup();
        group.setChapterNumber(1);
        group.getPairs().add(new VerseTokensPair(1));
        group.getPairs().add(new VerseTokensPair(2));
        group.getPairs().add(new VerseTokensPair(3));
        group.getPairs().add(new VerseTokensPair(4));
        mockMvc.perform(MockMvcRequestBuilders.post("/morphological/tokens").contentType(contentType).content(json(group)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(13)));
    }

    @Test(dependsOnMethods = "getTokens")
    public void getAllTokens() throws Exception {
        VerseTokenPairGroup group = new VerseTokenPairGroup();
        group.setChapterNumber(1);
        mockMvc.perform(MockMvcRequestBuilders.post("/morphological/tokens").contentType(contentType).content(json(group)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(29)));
    }

    @Test(dependsOnMethods = "getAllTokens")
    public void getToken() throws Exception {
        final String path = String.format("/morphological/chapter/%s/verse/%s/token/%s", FIRST_CHAPTER_NUMBER, FIRST_VERSE_NUMBER, FIRST_TOKEN_NUMBER);
        mockMvc.perform(MockMvcRequestBuilders.get(path))
                .andExpect(content().contentType(contentType))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName", Matchers.equalTo(DISPLAY_NAME)))
                .andExpect(jsonPath("$.locations", Matchers.hasSize(1)))
                .andExpect(jsonPath("$.locations[0].startIndex", Matchers.equalTo(0)))
                .andExpect(jsonPath("$.locations[0].endIndex", Matchers.equalTo(3)));
    }

    @Test(dependsOnMethods = "getToken")
    public void saveToken() throws Exception {
        final ArabicWord tokenWord = token.tokenWord();

        final Location location = token.getLocations().get(0);
        location.setEndIndex(1);
        ArabicWord arabicWord = ArabicWord.getSubWord(tokenWord, location.getStartIndex(), location.getEndIndex());
        location.setText(arabicWord.toUnicode());
        location.setDerivedText(arabicWord.toUnicode());
        location.setWordType(WordType.PARTICLE);
        List<AbstractProperties> properties = location.getProperties();
        final ParticleProperties particleProperties = (ParticleProperties) properties.get(0);
        particleProperties.setPartOfSpeech(ParticlePartOfSpeechType.GENITIVE_PARTICLE);

        Location newLocation = new Location(location.getChapterNumber(), location.getVerseNumber(), location.getTokenNumber(), 2, WordType.NOUN);
        newLocation.setStartIndex(1);
        newLocation.setEndIndex(arabicWord.getLength());
        arabicWord = ArabicWord.getSubWord(tokenWord, newLocation.getStartIndex(), newLocation.getEndIndex());
        newLocation.setText(arabicWord.toUnicode());
        newLocation.setDerivedText(arabicWord.toUnicode());
        properties = newLocation.getProperties();
        final NounProperties nounProperties = (NounProperties) properties.get(0);
        nounProperties.setPartOfSpeech(NounPartOfSpeechType.NOUN);
        nounProperties.setStatus(NounStatus.GENITIVE);

        token.addLocation(newLocation);

        final String path = "/morphological/saveToken";
        final ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(contentType).content(json(token)));
        resultActions.andExpect(jsonPath("$.displayName", Matchers.equalTo(DISPLAY_NAME)))
                .andExpect(jsonPath("$.locations", Matchers.hasSize(2)))
                .andExpect(jsonPath("$.locations[1].displayName", Matchers.equalTo("1:1:1:2")));
    }

    @Test(dependsOnMethods = "saveToken")
    public void createMorphologicalEntry() throws Exception {
        MorphologicalEntry morphologicalEntry = new MorphologicalEntry();
        morphologicalEntry.setForm(NamedTemplate.FORM_I_CATEGORY_A_GROUP_U_TEMPLATE);
        morphologicalEntry.setRootLetters(new RootLetters(ArabicLetterType.NOON, ArabicLetterType.SAD, ArabicLetterType.RA));
        morphologicalEntry.getVerbalNouns().add(VerbalNoun.VERBAL_NOUN_V1);
        morphologicalEntry.setShortTranslation("To help");
        morphologicalEntry.initDisplayName();

        final String path = "/morphological/morphologicalEntry/create";
        final ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(contentType)
                .content(json(morphologicalEntry)));
        resultActions.andExpect(jsonPath("$.id", Matchers.equalTo(morphologicalEntry.getId())))
                .andExpect(jsonPath("$.displayName", Matchers.equalTo(morphologicalEntry.getDisplayName())))
                .andExpect(jsonPath("$.rootLetters.displayName", Matchers.equalTo(morphologicalEntry.getRootLetters().getDisplayName())))
                .andExpect(jsonPath("$.groupTag", Matchers.equalTo(morphologicalEntry.getRootLetters().getDisplayName())));
    }

    @Test(dependsOnMethods = "createMorphologicalEntry")
    public void findMorphologicalEntry() throws Exception {
        final NamedTemplate namedTemplate = NamedTemplate.FORM_I_CATEGORY_A_GROUP_U_TEMPLATE;
        final RootLetters rootLetters = new RootLetters(ArabicLetterType.NOON, ArabicLetterType.SAD, ArabicLetterType.RA);
        final String displayName = String.format("%s:%s", namedTemplate.name(), rootLetters.getDisplayName());
        final String path = "/morphological/morphologicalEntry/find";
        final ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get(path).contentType(contentType)
                .param("displayName", displayName));
        resultActions.andExpect(jsonPath("$.id", Matchers.notNullValue()))
                .andExpect(jsonPath("$.displayName", Matchers.equalTo(displayName)))
                .andExpect(jsonPath("$.groupTag", Matchers.equalTo(rootLetters.getDisplayName())))
                .andExpect(jsonPath("$.form", Matchers.equalTo(namedTemplate.name())));
    }

    @SuppressWarnings("unchecked")
    private String json(Object o) throws IOException {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
        this.mappingJackson2HttpMessageConverter.write(o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
        return mockHttpOutputMessage.getBodyAsString();
    }
}
