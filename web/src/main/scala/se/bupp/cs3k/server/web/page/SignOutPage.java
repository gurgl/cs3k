/*
 * $Id: SignOut.java 5394 2006-04-16 13:36:52 +0000 (Sun, 16 Apr 2006)
 * jdonnerstag $ $Revision: 5877 $ $Date: 2006-04-16 13:36:52 +0000 (Sun, 16 Apr
 * 2006) $
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package se.bupp.cs3k.server.web.page;

import org.apache.wicket.Page;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import se.bupp.cs3k.server.web.page.AbstractBasePage;


public class SignOutPage extends AbstractBasePage {

  public static final String REDIRECTPAGE_PARAM = "redirectpage";

  @SuppressWarnings("unchecked")
  public SignOutPage(final PageParameters parameters) {
    String page = parameters.get(REDIRECTPAGE_PARAM).toOptionalString();
    Class<? extends Page> pageClass;
    if (page != null) {
      try {
        pageClass = (Class<? extends Page>) Class.forName(page);
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
    } else {
      pageClass = getApplication().getHomePage();
    }
    getSession().invalidate();
    setResponsePage(pageClass);
  }
}
