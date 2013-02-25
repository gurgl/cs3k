package se.bupp.cs3k.server.web.component.generic

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-02-25
 * Time: 00:33
 * To change this template use File | Settings | File Templates.
 */
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn
import org.apache.wicket.extensions.markup.html.repeater.data.table.export.IExportableColumn
import org.apache.wicket.markup.html.basic.Label
import org.apache.wicket.markup.repeater.Item
import org.apache.wicket.model.IModel
import org.apache.wicket.model.PropertyModel
import se.bupp.cs3k.server.web.generic.{Fodel}

/**
 * A convenience implementation of column that adds a label to the cell whose model is determined by
 * the provided wicket property expression (same as used by {@link PropertyModel}) that is evaluated
 * against the current row's model object
 * <p>
 * Example
 *
 * <pre>
 * columns[0] = new PropertyColumn(new Model&lt;String&gt;(&quot;First Name&quot;), &quot;name.first&quot;);
 * </pre>
 *
 * The above will attach a label to the cell with a property model for the expression
 * &quot;name.first&quot;
 *
 * @see PropertyModel
 *
 * @author Igor Vaynberg ( ivaynberg )
 * @param <T>
 *         The Model object type
 * @param <S>
 *         the type of the sort property
 */

class FodelPropertyColumn[T, S, P](displayModel: IModel[String], sortProperty: S, val propertyExpression: Fodel.PropSetGet[T,P]) extends AbstractColumn[T, S](displayModel, sortProperty) with IExportableColumn[T, S, AnyRef] {
  /**
   * Creates a property column that is also sortable
   *
   * @param displayModel
     * display model
   * @param sortProperty
     * sort property
   * @param propertyExpression
     * wicket property expression used by PropertyModel
   */

  /**
   * Creates a non sortable property column
   *
   * @param displayModel
     * display model
   * @param propertyExpression
     * wicket property expression
   * @see PropertyModel
   */
  def this(displayModel: IModel[String], propertyExpression: Fodel.PropSetGet[T,P]) = {
    this(displayModel,null.asInstanceOf[S],propertyExpression)

  }

  /**
   * Implementation of populateItem which adds a label to the cell whose model is the provided
   * property expression evaluated against rowModelObject
   *
   * @see ICellPopulator#populateItem(Item, String, IModel)
   */
  def populateItem(item: Item[ICellPopulator[T]], componentId: String, rowModel: IModel[T]) {
    item.add(new Label(componentId, createLabelModel(rowModel)))
  }

  /**
   * Factory method for generating a model that will generated the displayed value. Typically the
   * model is a property model using the {@link #propertyExpression} specified in the constructor.
   *
   * @param rowModel
   * @return model
   * @deprecated
     * since 6.2.0, scheduled for removal in 7.0.0. Please use { @link #getDataModel(org.apache.wicket.model.IModel)}
   *                                                                   instead.
   */
  protected def createLabelModel(rowModel: IModel[T]): IModel[_] = {
    return getDataModel(rowModel)
  }

  /**
   * @return wicket property expression
   */
  def getPropertyExpression: Fodel.PropSetGet[T,P]= {
    propertyExpression
  }

  /**
   * Factory method for generating a model that will generated the displayed value. Typically the
   * model is a property model using the {@link #propertyExpression} specified in the constructor.
   *
   * @param rowModel
   * @return model
   */
  def getDataModel(rowModel: IModel[T]) : IModel[AnyRef] = {
    //val propertyModel: PFodel[T,P] = new PFodel[T,P](rowModel, () => rowModel.getObject, )

    val fkn = (p:AnyRef) => { propertyExpression._2.apply(rowModel.getObject, p.asInstanceOf[P]) ; () } ;
    //
      new Fodel[AnyRef](
      () => propertyExpression._1(rowModel.getObject).asInstanceOf[AnyRef], fkn)
  }

  //private final var propertyExpression: Fodel.SetGet[T] = null
}