package com.alphasystem.morphologicalanalysis.rest;

import com.alphasystem.morphologicalanalysis.common.model.VerseTokenPairGroup;
import com.alphasystem.morphologicalanalysis.morphology.model.MorphologicalEntry;
import com.alphasystem.morphologicalanalysis.morphology.repository.MorphologicalEntryRepository;
import com.alphasystem.morphologicalanalysis.util.MorphologicalAnalysisRepositoryUtil;
import com.alphasystem.morphologicalanalysis.wordbyword.model.Chapter;
import com.alphasystem.morphologicalanalysis.wordbyword.model.Token;
import com.alphasystem.morphologicalanalysis.wordbyword.model.Verse;
import com.alphasystem.morphologicalanalysis.wordbyword.repository.ChapterRepository;
import com.alphasystem.morphologicalanalysis.wordbyword.repository.TokenRepository;
import com.alphasystem.morphologicalanalysis.wordbyword.repository.VerseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

/**
 * @author sali
 */
@RestController
@RequestMapping("/morphological")
public class MorphologicalAnalysisRestController {

    @Autowired private MorphologicalAnalysisRepositoryUtil repositoryUtil;
    @Autowired private ChapterRepository chapterRepository;
    @Autowired private VerseRepository verseRepository;
    @Autowired private TokenRepository tokenRepository;
    @Autowired private MorphologicalEntryRepository morphologicalEntryRepository;

    @RequestMapping(value = "/chapters", method = RequestMethod.GET)
    public List<Chapter> findAllChapters() {
        return repositoryUtil.findAllChapters();
    }

    @RequestMapping(value = "/chapter/{chapterNumber}", method = RequestMethod.GET)
    public Chapter findByChapterNumber(@PathVariable(name = "chapterNumber") int chapterNumber) {
        return chapterRepository.findByChapterNumber(chapterNumber);
    }

    @RequestMapping(value = "/chapter/{chapterNumber}/verse/{verseNumber}", method = RequestMethod.GET)
    public Verse findByChapterNumberAndVerseNumber(@PathVariable(name = "chapterNumber") int chapterNumber,
                                                   @PathVariable(name = "verseNumber") int verseNumber) {
        return verseRepository.findByChapterNumberAndVerseNumber(chapterNumber, verseNumber);
    }

    @RequestMapping(value = "/chapter/{chapterNumber}/verseFrom/{verseNumberFrom}/verseTo/{verseNumberTo}", method = RequestMethod.GET)
    public List<Verse> findVerseRange(@PathVariable(name = "chapterNumber") int chapterNumber,
                                      @PathVariable(name = "verseNumberFrom") int verseNumberFrom,
                                      @PathVariable(name = "verseNumberTo") int verseNumberTo) {
        return verseRepository.findByChapterNumberAndVerseNumberBetween(chapterNumber, verseNumberFrom - 1, verseNumberTo);
    }

    @RequestMapping(value = "/chapter/{chapterNumber}/verse/{verseNumber}/token/{tokenNumber}", method = RequestMethod.GET)
    public Token getToken(@PathVariable(name = "chapterNumber") int chapterNumber,
                          @PathVariable(name = "verseNumber") int verseNumber,
                          @PathVariable(name = "tokenNumber") int tokenNumber) {
        return tokenRepository.findByChapterNumberAndVerseNumberAndTokenNumber(chapterNumber, verseNumber, tokenNumber);
    }

    @RequestMapping(value = "/chapter/{chapterNumber}/verse/{verseNumber}/token/{tokenNumber}/next",
            consumes = APPLICATION_JSON_UTF8_VALUE, produces = APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.GET)
    public Token getNextToken(@PathVariable(name = "chapterNumber") int chapterNumber,
                              @PathVariable(name = "verseNumber") int verseNumber,
                              @PathVariable(name = "tokenNumber") int tokenNumber) {
        return repositoryUtil.getNextToken(new Token(chapterNumber, verseNumber, tokenNumber, ""));
    }

    @RequestMapping(value = "/saveToken", method = RequestMethod.POST)
    public Token saveToken(@RequestBody Token token) {
        return tokenRepository.save(token);
    }

    @RequestMapping(value = "/tokens", method = RequestMethod.POST, consumes = APPLICATION_JSON_UTF8_VALUE, produces = APPLICATION_JSON_UTF8_VALUE)
    public List<Token> getTokens(@RequestBody VerseTokenPairGroup group) {
        return repositoryUtil.getTokens(group);
    }

    @RequestMapping(value = "/morphologicalEntry/find", method = RequestMethod.GET, consumes = APPLICATION_JSON_UTF8_VALUE, produces = APPLICATION_JSON_UTF8_VALUE)
    public MorphologicalEntry findMorphologicalEntry(@RequestParam String displayName) {
        return morphologicalEntryRepository.findByDisplayName(displayName);
    }

}
