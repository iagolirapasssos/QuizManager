package io.quizmanager;

import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.runtime.*;
import com.google.appinventor.components.common.*;
import com.google.appinventor.components.runtime.util.YailList;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections; 

import android.util.Log;

@DesignerComponent(
        version = 1,
        description = "Quiz Manager Extension<br/><br/>" +
                      "This extension provides methods for managing a quiz game.",
        category = ComponentCategory.EXTENSION,
        nonVisible = true,
        iconName = "images/extension.png"
)
@SimpleObject(external = true)
public class QuizManager extends AndroidNonvisibleComponent {

    private List<Question> questions = new ArrayList<>();
    private int score;
    private int currentQuestionIndex;
    private long timeLimit;
    private final int NEGATIVE_MARK;
    
    public QuizManager(ComponentContainer container) {
        super(container.$form());
        
        score = 0;
        currentQuestionIndex = -1;
        timeLimit = 0;
        NEGATIVE_MARK = -1;
    }

    @SimpleFunction(description = "Add a question with options, the index of the correct answer, an optional image, a hint, and an explanation for the answer.")
    public void AddQuestion(String questionText, YailList options, int correctAnswerIndex, String imageUrl, String hint, String explanation) {
        questions.add(new Question(questionText, options.toStringArray(), correctAnswerIndex - 1, imageUrl, hint, explanation));
        Log.i("QuizManager", "Added question: " + questionText + " with image URL: " + imageUrl);
    }
    
    @SimpleFunction(description = "Get the current question image URL or asset name.")
    public String GetCurrentQuestionImage() {
    	Log.i("QuizManager",  currentQuestionIndex + " >= 0 && " + currentQuestionIndex + " < " + questions.size());
        if (currentQuestionIndex >= 0 && currentQuestionIndex < questions.size()) {
        	Log.i("QuizManager", "GetCurrentQuestionImage: "  + questions.get(currentQuestionIndex).getImageUrl());
            return questions.get(currentQuestionIndex).getImageUrl();
        } else {
            return "";
        }
    }

    @SimpleFunction(description = "Get the current question text.")
    public String GetCurrentQuestion() {
//    	Log.i("QuizManager",  currentQuestionIndex + " >= 0 && " + currentQuestionIndex + " < " + questions.size());
        if (currentQuestionIndex >= 0 && currentQuestionIndex < questions.size()) {
        	Log.i("QuizManager", "GetCurrentQuestion: " + questions.get(currentQuestionIndex).getText());
            return questions.get(currentQuestionIndex).getText();
        } else {
            return "";
        }
    }

    @SimpleFunction(description = "Get the current question options as a list.")
    public YailList GetCurrentOptions() {
    	Log.i("QuizManager",  currentQuestionIndex + " >= 0 && " + currentQuestionIndex + " < " + questions.size());
        if (currentQuestionIndex >= 0 && currentQuestionIndex < questions.size()) {
        	Log.i("QuizManager", "GetCurrentOptions: "  + YailList.makeList(questions.get(currentQuestionIndex).getOptions()));
            return YailList.makeList(questions.get(currentQuestionIndex).getOptions());
        } else {
            return new YailList();
        }
    }

    @SimpleFunction(description = "Move to the next question. Returns false if there are no more questions.")
    public boolean NextQuestion() {
        if (currentQuestionIndex < questions.size() - 1) {
            currentQuestionIndex++;
            updateQuizProgress();
            return true;
        } else {
            return false;
        }
    }

    @SimpleFunction(description = "Check the answer for the current question and update the score. Returns true if the answer is correct.")
    public boolean CheckAnswer(int answerIndex) {
        if (currentQuestionIndex >= 0 && currentQuestionIndex < questions.size()) {
            Question currentQuestion = questions.get(currentQuestionIndex);
            if (currentQuestion.getCorrectAnswerIndex() == answerIndex - 1) {
                score++;
                QuestionAnswered(true, "");
                updateQuizProgress();
                return true;
            } else {
                score += NEGATIVE_MARK; // Apply negative marking
                QuestionAnswered(false, currentQuestion.getExplanation());
                updateQuizProgress();
                return false;
            }
        }
        return false;
    }

    @SimpleFunction(description = "Get the current score.")
    public int GetScore() {
        return score;
    }

    @SimpleFunction(description = "Reset the quiz to the first question and reset the score.")
    public void ResetQuiz() {
        currentQuestionIndex = -1; // Inicializar com o índice da primeira pergunta
        score = 0;
        updateQuizProgress();
        Log.i("QuizManager", "Quiz reset. Score is zeroed and question index set to 0.");
    }
    
    @SimpleFunction(description = "Get the total number of questions.")
    public int TotalQuestions() {
        return questions.size();
    }
    
    @SimpleFunction(description = "Get the current question index.")
    public int GetCurrentQuestionIndex() {
        return currentQuestionIndex + 1;
    }
    
    @SimpleFunction(description = "Check if the quiz is finished.")
    public boolean IsQuizFinished() {
        return currentQuestionIndex >= questions.size();
    }

    @SimpleFunction(description = "Get the correct answer for the current question.")
    public String GetCorrectAnswer() {
        if (currentQuestionIndex >= 0 && currentQuestionIndex < questions.size()) {
            int correctIndex = questions.get(currentQuestionIndex).getCorrectAnswerIndex();
            return questions.get(currentQuestionIndex).getOptions()[correctIndex];
        } else {
            return "";
        }
    }

    @SimpleFunction(description = "Get the question text by index.")
    public String GetQuestionByIndex(int index) {
        index--; // Ajusta para base zero
        if (index >= 0 && index < questions.size()) {
            return questions.get(index).getText();
        } else {
            return "Index out of bounds";
        }
    }

    @SimpleFunction(description = "Get the options for a question by index.")
    public YailList GetOptionsByIndex(int index) {
        index--; // Ajusta para base zero
        if (index >= 0 && index < questions.size()) {
            return YailList.makeList(questions.get(index).getOptions());
        } else {
            return new YailList();
        }
    }

    @SimpleFunction(description = "Shuffle the order of the questions.")
    public void ShuffleQuestions() {
        Collections.shuffle(questions);
        ResetQuiz();
    }
    
    @SimpleFunction(description = "Set a time limit for each question.")
    public void SetTimeLimit(long limit) {
        timeLimit = limit;
    }
    
    @SimpleFunction(description = "Get the user's score as a percentage.")
    public int GetScorePercentage() {
        if (questions.size() > 0) {
            return (int) (((double) score / questions.size()) * 100);
        }
        return 0;
    }
    
    @SimpleFunction(description = "Get the number of incorrect answers.")
    public int GetIncorrectAnswers() {
        return (currentQuestionIndex + 1) - score;
    }

    @SimpleFunction(description = "Request a hint for the current question, if available.")
    public String HintRequest() {
        if (currentQuestionIndex >= 0 && currentQuestionIndex < questions.size()) {
            return questions.get(currentQuestionIndex).getHint();
        }
        return "No hint available.";
    }

    @SimpleFunction(description = "Skip the current question and move to the next one.")
    public boolean SkipQuestion() {
        if (currentQuestionIndex >= 0 && currentQuestionIndex < questions.size()) {
            questions.get(currentQuestionIndex).incrementSkipCount(); // Incrementa o contador de pulos
            return NextQuestion(); // Chama NextQuestion() para avançar
        }
        return false;
    }    
    
    /*
     * EVENTS
     */
    
    @SimpleEvent(description = "Triggered when progress is made in the quiz. Returns the completion percentage as an integer.")
    public void QuizProgress(int percentComplete) {
        EventDispatcher.dispatchEvent(this, "QuizProgress", percentComplete);
    }

    @SimpleEvent(description = "Triggered when a question is answered. The parameters 'correct' indicates if the answer was correct, and 'explanation' provides the explanation.")
    public void QuestionAnswered(boolean correct, String explanation) {
        EventDispatcher.dispatchEvent(this, "QuestionAnswered", correct, explanation);
    }
    
    /*
     * PRIVATE METHODS
     */
    
    private void updateQuizProgress() {
        // Assegure-se de que não estamos dividindo por zero
        if (questions.size() > 0) {
            int percentComplete = (int) (((double) (currentQuestionIndex + 1) / questions.size()) * 100);
            QuizProgress(percentComplete);
        }
    }
    
    /*
     * PRIVATE CLASS
     */

    private class Question {
    	private String text;
        private String[] options;
        private int correctAnswerIndex;
        private String imageUrl;
        private String hint; // Dica para a pergunta
        private int skipCount; // Contador de quantas vezes a pergunta foi pulada
        private String explanation;

        public Question(String text, String[] options, int correctAnswerIndex, String imageUrl, String hint, String explanation) {
            this.text = text;
            this.options = options;
            this.correctAnswerIndex = correctAnswerIndex;
            this.imageUrl = imageUrl;
            this.hint = hint;
            this.explanation = explanation; // Set the explanation
        }

        public String getText() {
            return text;
        }

        public String[] getOptions() {
            return options;
        }

        public int getCorrectAnswerIndex() {
            return correctAnswerIndex;
        }

        public String getImageUrl() {
            return imageUrl;
        }
        
        public String getHint() {
            return hint;
        }

        public void incrementSkipCount() {
            skipCount++;
        }

        public int getSkipCount() {
            return skipCount;
        }
        
        // Getter for explanation
        public String getExplanation() {
            return explanation;
        }
    }
}
