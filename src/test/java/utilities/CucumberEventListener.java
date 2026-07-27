package utilities;


import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class CucumberEventListener implements ConcurrentEventListener {


    private static final Logger logger =
            LogManager.getLogger(CucumberEventListener.class);



    @Override
    public void setEventPublisher(EventPublisher publisher) {


        publisher.registerHandlerFor(
                TestCaseStarted.class,
                this::scenarioStarted
        );


        publisher.registerHandlerFor(
                TestStepStarted.class,
                this::stepStarted
        );


        publisher.registerHandlerFor(
                TestStepFinished.class,
                this::stepFinished
        );


        publisher.registerHandlerFor(
                TestCaseFinished.class,
                this::scenarioFinished
        );


    }



    private void scenarioStarted(TestCaseStarted event){


        logger.info(
                "========== Scenario Started =========="
        );


        logger.info(
                "Scenario : "
                        + event.getTestCase()
                        .getName()
        );

    }





    private void stepStarted(TestStepStarted event){


        if(event.getTestStep()
                instanceof PickleStepTestStep step){


            logger.info(
                    "STEP STARTED : "
                            + step.getStep()
                            .getKeyword()
                            + step.getStep()
                            .getText()
            );

        }

    }





    private void stepFinished(TestStepFinished event){


        if(event.getTestStep()
                instanceof PickleStepTestStep step){


            String stepName =
                    step.getStep()
                            .getKeyword()
                            +
                            step.getStep()
                                    .getText();



            if(event.getResult()
                    .getStatus()
                    .equals(Status.PASSED)){


                logger.info(
                        "STEP PASSED : "
                                + stepName
                );


            }
            else if(event.getResult()
                    .getStatus()
                    .equals(Status.FAILED)){


                logger.error(
                        "STEP FAILED : "
                                + stepName
                );


                logger.error(
                        "Error : "
                                + event.getResult()
                                .getError()
                );


            }

        }

    }





    private void scenarioFinished(TestCaseFinished event){


        if(event.getResult()
                .getStatus()
                .equals(Status.PASSED)){


            logger.info(
                    "Scenario Passed"
            );


        }
        else{


            logger.error(
                    "Scenario Failed"
            );


        }


        logger.info(
                "========== Scenario Finished =========="
        );

    }


}