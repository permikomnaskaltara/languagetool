/* LanguageTool, a natural language style checker
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package org.languagetool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.languagetool.JLanguageTool.ParagraphHandling;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.language.BritishEnglish;
import org.languagetool.language.English;
import org.languagetool.rules.*;
import org.languagetool.rules.patterns.PatternToken;
import org.languagetool.rules.patterns.PatternRule;

public class JLanguageToolTest extends TestCase {

  // used on http://languagetool.org/java-api/
  public void demoCodeForHomepage() throws IOException {
    JLanguageTool langTool = new JLanguageTool(new BritishEnglish());
    List<RuleMatch> matches = langTool.check("A sentence with a error in the Hitchhiker's Guide tot he Galaxy");
    for (RuleMatch match : matches) {
      System.out.println("Potential error at line " +
          match.getLine() + ", column " +
          match.getColumn() + ": " + match.getMessage());
      System.out.println("Suggested correction: " +
          match.getSuggestedReplacements());
    }
  }

  // used on http://languagetool.org/java-spell-checker/
  public void spellCheckerDemoCodeForHomepage() throws IOException {
    JLanguageTool langTool = new JLanguageTool(new BritishEnglish());
    for (Rule rule : langTool.getAllRules()) {
      if (!rule.isDictionaryBasedSpellingRule()) {
        langTool.disableRule(rule.getId());
      }
    }
    List<RuleMatch> matches = langTool.check("A speling error");
    for (RuleMatch match : matches) {
      System.out.println("Potential typo at line " +
          match.getLine() + ", column " +
          match.getColumn() + ": " + match.getMessage());
      System.out.println("Suggested correction(s): " +
          match.getSuggestedReplacements());
    }
  }

  public void testEnglish() throws IOException {
    JLanguageTool tool = new JLanguageTool(new English());
    assertEquals(0, tool.check("A test that should not give errors.").size());

    //more error-free sentences to deal with possible regressions
    assertEquals(0, tool.check("As long as you have hope, a chance remains.").size());
    assertEquals(0, tool.check("A rolling stone gathers no moss.").size());
    assertEquals(0, tool.check("Hard work causes fitness.").size());
    assertEquals(0, tool.check("Gershwin overlays the slow blues theme from section B in the final “Grandioso.”").size());
    assertEquals(0, tool.check("Making ingroup membership more noticeable increases cooperativeness.").size());
    assertEquals(0, tool.check("Dog mushing is more of a sport than a true means of transportation.").size());
    assertEquals(0, tool.check("No one trusts him any more.").size());
    assertEquals(0, tool.check("A member of the United Nations since 1992, Azerbaijan was elected to membership in the newly established Human Rights Council by the United Nations General Assembly on May 9, 2006 (the term of office began on June 19, 2006).").size());
    assertEquals(0, tool.check("Anatomy and geometry are fused in one, and each does something to the other.").size());
    assertEquals(0, tool.check("Certain frogs that lay eggs underground have unpigmented eggs.").size());
    assertEquals(0, tool.check("It's a kind of agreement in which each party gives something to the other, Jack said.").size());
    assertEquals(0, tool.check("Later, you shall know it better.").size());
    assertEquals(0, tool.check("And the few must win what the many lose, for the opposite arrangement would not support markets as we know them at all, and is, in fact, unimaginable.").size());
    assertEquals(0, tool.check("He explained his errand, but without bothering much to make it plausible, for he felt something well up in him which was the reason why he had fled the army.").size());
    assertEquals(0, tool.check("I think it's better, and it's not a big deal.").size());

    assertEquals(1, tool.check("A test test that should give errors.").size());
    assertEquals(1, tool.check("I can give you more a detailed description.").size());
    assertTrue(tool.getAllRules().size() > 1000);
    assertEquals(0, tool.check("The sea ice is highly variable - frozen solid during cold, calm weather and broke...").size());
    assertTrue(tool.getAllRules().size() > 3);
    assertEquals(1, tool.check("I can give you more a detailed description.").size());
    tool.disableRule("MORE_A_JJ");
    assertEquals(0, tool.check("I can give you more a detailed description.").size());
    assertEquals(1, tool.check("I've go to go.").size());
    tool.disableCategory(Categories.TYPOS.getId());
    assertEquals(0, tool.check("I've go to go.").size());
  }

  public void testPositionsWithEnglish() throws IOException {
    JLanguageTool tool = new JLanguageTool(new AmericanEnglish());
    List<RuleMatch> matches = tool.check("A sentence with no period\n" +
        "A sentence. A typoh.");
    assertEquals(1, matches.size());
    RuleMatch match = matches.get(0);
    assertEquals(1, match.getLine());
    assertEquals(15, match.getColumn());
  }

  public void testPositionsWithEnglishTwoLineBreaks() throws IOException {
    JLanguageTool tool = new JLanguageTool(new AmericanEnglish());
    List<RuleMatch> matches = tool.check("This sentence.\n\n" +
        "A sentence. A typoh.");
    assertEquals(1, matches.size());
    RuleMatch match = matches.get(0);
    assertEquals(2, match.getLine());
    assertEquals(14, match.getColumn());   // TODO: should actually be 15, as in testPositionsWithEnglish()
  }

  public void testAnalyzedSentence() throws IOException {
    JLanguageTool tool = new JLanguageTool(new English());
    //test soft-hyphen ignoring:
    assertEquals("<S> This[this/DT,B-NP-singular|E-NP-singular] " +
        "is[be/VBZ,B-VP] a[a/DT,B-NP-singular] " +
        "test­ed[tested/JJ,test/VBD,test/VBN,test­ed/null,I-NP-singular] " +
        "sentence[sentence/NN,E-NP-singular].[./.,</S>,O]",
        tool.getAnalyzedSentence("This is a test\u00aded sentence.").toString());
    //test paragraph ends adding
    assertEquals("<S> </S><P/> ", tool.getAnalyzedSentence("\n").toString());
  }

  public void testParagraphRules() throws IOException {
    JLanguageTool tool = new JLanguageTool(new English());

    //run normally
    List<RuleMatch> matches1 = tool.check("(This is an quote.\n It ends in the second sentence.");
    assertEquals(2, matches1.size());
    assertEquals(2, tool.getSentenceCount());

    //run in a sentence-only mode
    List<RuleMatch> matches2 = tool.check("(This is an quote.\n It ends in the second sentence.", false, ParagraphHandling.ONLYNONPARA);
    assertEquals(1, matches2.size());
    assertEquals("EN_A_VS_AN", matches2.get(0).getRule().getId());
    assertEquals(1, tool.getSentenceCount());

    //run in a paragraph mode - single sentence
    List<RuleMatch> matches3 = tool.check("(This is an quote.\n It ends in the second sentence.", false, ParagraphHandling.ONLYPARA);
    assertEquals(1, matches3.size());
    assertEquals("EN_UNPAIRED_BRACKETS", matches3.get(0).getRule().getId());
    assertEquals(1, tool.getSentenceCount());

    //run in a paragraph mode - many sentences
    List<RuleMatch> matches4 = tool.check("(This is an quote.\n It ends in the second sentence.", true, ParagraphHandling.ONLYPARA);
    assertEquals(1, matches4.size());
    assertEquals("EN_UNPAIRED_BRACKETS", matches4.get(0).getRule().getId());
    assertEquals(2, tool.getSentenceCount());
  }

  public void testWhitespace() throws IOException {
    JLanguageTool tool = new JLanguageTool(new English());
    AnalyzedSentence raw = tool.getRawAnalyzedSentence("Let's do a \"test\", do you understand?");
    AnalyzedSentence cooked = tool.getAnalyzedSentence("Let's do a \"test\", do you understand?");
    //test if there was a change
    assertFalse(raw.equals(cooked));
    //see if nothing has been deleted
    assertEquals(raw.getTokens().length, cooked.getTokens().length);
    int i = 0;
    for (AnalyzedTokenReadings atr : raw.getTokens()) {
      assertEquals(atr.isWhitespaceBefore(),
          cooked.getTokens()[i].isWhitespaceBefore());
      i++;
    }
  }

  public void testOverlapFilter() throws IOException {
    Category category = new Category(new CategoryId("TEST_ID"), "test category");
    List<PatternToken> elements1 = Arrays.asList(new PatternToken("one", true, false, false));
    PatternRule rule1 = new PatternRule("id1", new English(), elements1, "desc1", "msg1", "shortMsg1");
    rule1.setSubId("1");
    rule1.setCategory(category);

    List<PatternToken> elements2 = Arrays.asList(new PatternToken("one", true, false, false), new PatternToken("two", true, false, false));
    PatternRule rule2 = new PatternRule("id1", new English(), elements2, "desc2", "msg2", "shortMsg2");
    rule2.setSubId("2");
    rule2.setCategory(category);

    JLanguageTool tool = new JLanguageTool(new English());
    tool.addRule(rule1);
    tool.addRule(rule2);

    List<RuleMatch> ruleMatches1 = tool.check("And one two three.");
    assertEquals("one overlapping rule must be filtered out", 1, ruleMatches1.size());
    assertEquals("msg1", ruleMatches1.get(0).getMessage());

    String sentence = "And one two three.";
    AnalyzedSentence analyzedSentence = tool.getAnalyzedSentence(sentence);
    List<Rule> bothRules = new ArrayList<Rule>(Arrays.asList(rule1, rule2));
    List<RuleMatch> ruleMatches2 = tool.checkAnalyzedSentence(ParagraphHandling.NORMAL, bothRules, 0, 0, 0, sentence, analyzedSentence);
    assertEquals("one overlapping rule must be filtered out", 1, ruleMatches2.size());
    assertEquals("msg1", ruleMatches2.get(0).getMessage());
  }
}
