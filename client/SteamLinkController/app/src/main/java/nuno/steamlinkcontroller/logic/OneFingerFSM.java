package nuno.steamlinkcontroller.logic;

public class OneFingerFSM
{
    private final int MAX_DELTA = 100;
    private OneFingerState oneFingerState = OneFingerState.INIT;
    private long lastEvTime = 0;

    public void updateState(boolean tapDown)
    {
        long curr = System.currentTimeMillis();
        long diff = curr - this.lastEvTime;

        switch (this.oneFingerState)
        {
            case INIT:
                if(tapDown)
                    this.oneFingerState = OneFingerState.FD1;
                break;
            case FD1:
                if(!tapDown && diff <= MAX_DELTA)
                    this.oneFingerState = OneFingerState.FU1;
                break;
            case FU1:
                if(tapDown && diff <= MAX_DELTA)
                    this.oneFingerState = OneFingerState.SD1;
                break;
            case SD1:
                if(!tapDown && diff <= MAX_DELTA)
                    this.oneFingerState = OneFingerState.SU1;
                break;
            case SU1:
                break;
        }

        this.lastEvTime = curr;
    }

    public OneFingerState getEventToSend()
    {
        long diff = System.currentTimeMillis() - this.lastEvTime;

        switch (this.oneFingerState)
        {
            case INIT:
                return OneFingerState.NULL;
            case FD1:
                if(diff <= MAX_DELTA)
                    return OneFingerState.NULL;
                else
                {
                    this.oneFingerState = OneFingerState.INIT;
                    return OneFingerState.NULL;
                }
            case FU1:
                if(diff <= MAX_DELTA)
                    return OneFingerState.NULL;
                else
                {
                    this.oneFingerState = OneFingerState.INIT;
                    return OneFingerState.FU1;
                }
            case SD1:
                if(diff <= MAX_DELTA)
                    return OneFingerState.NULL;
                else
                {
                    this.oneFingerState = OneFingerState.INIT;
                    return OneFingerState.SD1;
                }
            case SU1:
                this.oneFingerState = OneFingerState.INIT;
                return OneFingerState.SU1;
            default:
                return OneFingerState.NULL;
        }


    }
}
