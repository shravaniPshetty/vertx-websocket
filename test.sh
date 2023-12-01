#!/bin/bash

          for PROJECT_POM in $(find . -name pom.xml)
          do
            mvn clean verify -f ${PROJECT_POM} -ntp -B
          done

