package nuno.steamlinkcontroller.logic;

public class TwoFingerFSM
{
    private final int MAX_DELTA = 100;
    private TwoFingerState twoFingerState = TwoFingerState.INIT;
    private long lastEvTime = 0;

    public void updateState(boolean tapDown)
    {
        long curr = System.currentTimeMillis();
        long diff = curr - this.lastEvTime;

        switch (this.twoFingerState)
        {
            case INIT:
                if(tapDown)
                    this.twoFingerState = TwoFingerState.FD2;
                break;
            case FD2:
                if(!tapDown && diff <= MAX_DELTA)
                    this.twoFingerState = TwoFingerState.FU2;
                break;
            case FU2:
                break;
        }

        this.lastEvTime = curr;
    }

    public TwoFingerState getEventToSend()
    {
        long diff = System.currentTimeMillis() - this.lastEvTime;

        switch (this.twoFingerState)
        {
            case INIT:
                return TwoFingerState.NULL;
            case FD2:
                if(diff <= MAX_DELTA)
                    return TwoFingerState.NULL;
                else
                {
                    this.twoFingerState = TwoFingerState.INIT;
                    return TwoFingerState.FD2;
                }
            case FU2:
                this.twoFingerState = TwoFingerState.INIT;
                return TwoFingerState.FU2;
            default:
                return TwoFingerState.NULL;
        }


    }
}
