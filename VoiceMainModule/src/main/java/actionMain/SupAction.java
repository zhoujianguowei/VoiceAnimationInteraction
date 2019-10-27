package actionMain;
import com.example.voiceanimationinteraction.SpeechHandler;
import com.example.voiceanimationinteraction.SpeechMainActivity;

import utils.SpeechRecognizerWrapper;
import utils.SpeechSynthesizerWrapper;
/**
 * Created by Administrator on 2015/6/2.
 */
public class SupAction implements ActionImpl
{

    protected static SpeechMainActivity speechMain;
    protected static SpeechHandler recognizerHandler;
    protected static SpeechHandler synthesizerHandler;
    protected static String dictation;
    static SpeechSynthesizerWrapper synthesizerWrapper;
    static SpeechRecognizerWrapper recognizerWrapper;
    @Override
    public void handleDictation(SpeechMainActivity context, String dictation)
    {
        speechMain = context;
        this.dictation = dictation;
        recognizerHandler = new SpeechHandler(context, SpeechHandler
                .SPEECH_RECOGNIZER_FLAG);
        synthesizerHandler = new SpeechHandler(context, SpeechHandler
                .SPEECH_SYNTHESIER_FLAG);
        recognizerWrapper = recognizerWrapper.getSingleInstance(context,
                recognizerHandler);
        synthesizerWrapper = synthesizerWrapper.getSingleInstance(context,
                synthesizerHandler);
    }
}
