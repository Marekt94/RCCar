package com.example.marektomaslokalny.beastver200;

public interface ConnectionInterfaces {
    interface View{
        public void updateView();
        public String getUUID();
    }

    interface Controller{
        public void init();

    }
}
