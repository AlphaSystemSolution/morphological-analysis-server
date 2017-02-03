package com.alphasystem.morphologicalanalysis.rest;

import com.alphasystem.morphologicalanalysis.util.MorphologicalAnalysisRepositoryUtil;
import com.alphasystem.morphologicalanalysis.wordbyword.model.Chapter;
import com.alphasystem.morphologicalanalysis.wordbyword.model.Verse;
import com.alphasystem.morphologicalanalysis.wordbyword.repository.ChapterRepository;
import com.alphasystem.morphologicalanalysis.wordbyword.repository.VerseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author sali
 */
@RestController
@RequestMapping("/mongo")
public class MonoRestController {

    @Autowired private MorphologicalAnalysisRepositoryUtil repositoryUtil;
    @Autowired private ChapterRepository chapterRepository;
    @Autowired private VerseRepository verseRepository;

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

}
