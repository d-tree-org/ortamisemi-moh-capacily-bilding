package org.smartregister.chw.application;

public class ChwApplicationFlv extends DefaultChwApplicationFlv {
    @Override
    public boolean useThinkMd() {
        return false;
    }

    @Override
    public boolean hasP2P() {
        return false;
    }
    @Override
    public boolean hasWashCheck() {
        return false;
    }

    @Override
    public boolean hasJobAids() {
        return false;
    }

    @Override
    public boolean hasJobAidsVitaminAGraph() {
        return false;
    }

    @Override
    public boolean hasJobAidsDewormingGraph() {
        return false;
    }

    @Override
    public boolean hasChildrenMNPSupplementationGraph() {
        return false;
    }

    @Override
    public boolean hasJobAidsBreastfeedingGraph() {
        return false;
    }

    @Override
    public boolean hasJobAidsBirthCertificationGraph() {
        return false;
    }
}
