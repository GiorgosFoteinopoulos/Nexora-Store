package com.practice.nexora.advisor;

import java.util.List;

public interface AdvisorClient {


    NexaAnswer reply(List<ChatTurn> history, String userMessage);
}
