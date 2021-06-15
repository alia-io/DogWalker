package com.example.dogwalker.setupprofile;

public interface FragmentTracker {
    void goNext();
    void goPrevious();
    void saveFragment1(boolean dogOwner, boolean dogWalker, int ownerY, int ownerM, int ownerD, int walkerY, int walkerM, int walkerD);
    void finished();
}
