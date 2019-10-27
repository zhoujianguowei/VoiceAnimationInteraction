package actionMain;
import com.example.voiceanimationinteraction.SpeechMainActivity;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
public interface ActionImpl
{

    /**
     * 用于控制语音听写和语音合成两个进程的同步
     */
    ReentrantLock lock = new ReentrantLock();
    Condition condition = lock.newCondition();
    void handleDictation(SpeechMainActivity context, String dictation);
}
