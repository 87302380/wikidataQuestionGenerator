package de.ostfalia.teamprojekt.wwm.wikidatatest.questions;

import com.google.common.collect.ImmutableList;
import de.ostfalia.teamprojekt.wwm.wikidatatest.model.Question;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wikidata.wdtk.datamodel.interfaces.*;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class MaerchenFigurQuestion implements QuestionType {

    private static final Logger LOGGER = LoggerFactory.getLogger(MaerchenFigurQuestion.class);
    private static Map<String, Set<String>> MaerchenFigur = new HashMap<>();

    public MaerchenFigurQuestion() {
        if (MaerchenFigur.isEmpty()) {
            try (Scanner s = new Scanner(getClass().getClassLoader().getResourceAsStream("maerchenResources/maerchenList.csv"), "UTF-8")) {
                s.useDelimiter(",");
          //      s.nextLine(); // skip first line
                while (s.hasNext()) {
                    MaerchenFigur.put(s.next(), new HashSet<>(1000));
                    s.nextLine();
                }
            }
        }
    }

    @Override public boolean itemRelevant(final ItemDocument itemDocument) {
        for (StatementGroup sg : itemDocument.getStatementGroups()) {
            if (sg.getProperty().getId().equals("P1441")) {
                for (Statement s : sg.getStatements()) {
                    if (s.getClaim().getMainSnak() instanceof ValueSnak) {
                        Value v = ((ValueSnak) s.getClaim().getMainSnak()).getValue();
                        if (v instanceof ItemIdValue && MaerchenFigur.containsKey(((ItemIdValue) v).getId())) {
                            // german label might not exist
                            //LOGGER.log(Level.INFO, itemDocument.getLabels().get("de").getText() + ": " + ((ItemIdValue) v).getId());
                            LOGGER.info(itemDocument.getEntityId().getId() + " is of type " + ((ItemIdValue) v).getId());
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }


    @Override public Stream<Question> generateQuestions()  {

        Supplier<Question> questions = new maerchenQuestionSupplier();

        return Stream.generate(questions);
    }

    private class maerchenQuestionSupplier implements Supplier<Question> {

        @Override
        public Question get() {
            QuestionGenerator questionGenerator = null;
            try {
                questionGenerator = new QuestionGenerator();
            } catch (IOException e) {
                e.printStackTrace();
            }

            assert questionGenerator != null;
            return questionGenerator.getQuestion();

        }
    }

    @Override public void processItemDocument(final ItemDocument itemDocument) {
        for (StatementGroup sg : itemDocument.getStatementGroups()) {
            if (sg.getProperty().getId().equals("P1441")) {
                for (Statement s : sg.getStatements()) {
                    if (s.getClaim().getMainSnak() instanceof ValueSnak) {
                        Value v = ((ValueSnak) s.getClaim().getMainSnak()).getValue();
                        if (v instanceof ItemIdValue) {
                            String maerchen = ((ItemIdValue) v).getId();
                            MaerchenFigur.get(maerchen).add(itemDocument.getLabels().get("de").getText());
                            LOGGER.info(itemDocument.getLabels().get("de").getText() + ": " + maerchen);
                        }
                    }
                }
            }
        }
    }

    public class QuestionGenerator {

        private String maechenPath = "./src/main/resources/maerchenResources/maerchenList.csv";
        private String maechenFigurPath = "./src/main/resources/maerchenResources/maerchenFigurList.csv";
        private String anserPath = "./src/main/resources/maerchenResources/antwort.csv";

        Map<String, List<String>> maerchenList = new HashMap<>();
        Map<String, List<String>> map = new HashMap<>();

        private QuestionGenerator() throws IOException {

            String[] maerchen = this.dataRead(maechenPath);
            String[] maerchenFigur = dataRead(maechenFigurPath);
            String[] anser = dataRead(anserPath);

            mapGenerator(this.maerchenList,mergeArray(maerchen,maerchenFigur));
            mapGenerator(this.map,anser);

        }

        private Question getQuestion(){

            List<String> enty = entyGenerator(this.map);
            List<String> option = optionGenerator(enty,this.map);

            idToLabel(enty,this.maerchenList);
            idToLabel(option,this.maerchenList);

            return questionLoad(enty,option);

        }

        private void mapGenerator(Map<String, List<String>> map, String[] anser){

            for (String a : anser){
                String[] enty = a.split(",");
                if (map.get(enty[0])==null){
                    List<String> anserList = new ArrayList<>();
                    anserList.add(enty[1]);
                    map.put(enty[0],anserList);
                }else {
                    map.get(enty[0]).add(enty[1]);
                }
            }
        }

        private void idToLabel(List<String> list, Map<String, List<String>> maerchenList){
            for (int i = 0;i<list.size();i++){
                if (maerchenList.containsKey(list.get(i))){
                    list.set(i, maerchenList.get(list.get(i)).get(0));
                }
            }
        }

        private List<String> optionGenerator(List<String> entylist, Map<String, List<String>> map){

            String[] key = map.keySet().toArray(new String[0]);

            Random random = new Random();

            List<String> optionlist = new ArrayList<>();

            optionlist.add(map.get(entylist.get(0)).get(random.nextInt(map.get(entylist.get(0)).size())));

            while (optionlist.size()<4){

                boolean isAnewoption = true;

                String keyValue = key[random.nextInt(key.length)];
                String option = map.get(keyValue).get(random.nextInt(map.get(keyValue).size()));

                for (String string:optionlist){
                    if (option.equals(string)){
                        isAnewoption = false;
                        break;
                    }
                }
                if (isAnewoption){
                    optionlist.add(option);
                }

            }

            return optionlist;
        }

        private List<String> entyGenerator(Map<String, List<String>> map){
            String[] key = map.keySet().toArray(new String[0]);

            Random random = new Random();

            List<String> entylist = new ArrayList<>();

            while (entylist.size()<4){

                boolean isAnewoption = true;
                String option = key[random.nextInt(key.length)];
                for (String string : entylist){
                    if (option.equals(string) ){
                        isAnewoption = false;
                    }
                }
                if (isAnewoption){
                    entylist.add(option);
                }
            }

            return entylist;
        }


        private String[] mergeArray(String[] a , String[] b){
            String[] c= new String[a.length+b.length];
            System.arraycopy(a, 0, c, 0, a.length);
            System.arraycopy(b, 0, c, a.length, b.length);
            return c;
        }

        String[] dataRead(String input) throws IOException {
            File file = new File(input);
            String content= FileUtils.readFileToString(file,"UTF-8");

            return content.split("\n");
        }

        private Question questionLoad(List<String> entylist, List<String> optionlist){

            String text = entylist.get(0)+" kommt aus welchen folfenden Märchen?";

            return new Question(text, ImmutableList.copyOf(optionlist));
        }

    }

}
