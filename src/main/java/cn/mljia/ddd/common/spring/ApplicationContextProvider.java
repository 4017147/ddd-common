//   Copyright 2012,2013 Vaughn Vernon
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.

package cn.mljia.ddd.common.spring;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class ApplicationContextProvider implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    public static ApplicationContextProvider instance() {
        return (ApplicationContextProvider) applicationContext.getBean("applicationContextProvider");
    }

    public ApplicationContextProvider() {
        super();
    }

    public synchronized ApplicationContext applicationContext() {
        return ApplicationContextProvider.applicationContext;
    }

    @Override
    public synchronized void setApplicationContext(ApplicationContext anApplicationContext) throws BeansException {
        if (ApplicationContextProvider.applicationContext == null) {
            ApplicationContextProvider.applicationContext = anApplicationContext;
        }
    }
}
