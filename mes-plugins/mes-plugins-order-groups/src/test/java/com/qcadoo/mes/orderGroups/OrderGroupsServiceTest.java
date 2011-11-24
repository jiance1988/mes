package com.qcadoo.mes.orderGroups;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.utils.NumberGeneratorService;

public class OrderGroupsServiceTest {

    private OrderGroupsService orderGroupsService;

    private Entity order = null;

    private Entity orderGroup = null;

    private EntityList ordersList = null;

    private long now = new Date().getTime();

    private DataDefinition dataDefinition = null;

    private ViewDefinitionState view = null;

    private ComponentState numberComponent = null;

    private ComponentState nameComponent = null;

    private NumberGeneratorService numberGeneratorService = null;

    private TranslationService translationService = null;

    @Before
    public final void init() {
        orderGroupsService = new OrderGroupsService();

        mockModel();
        mockView();

        ReflectionTestUtils.setField(orderGroupsService, "translationService", translationService);
        ReflectionTestUtils.setField(orderGroupsService, "numberGeneratorService", numberGeneratorService);
    }

    private void mockView() {
        numberComponent = mock(ComponentState.class);
        nameComponent = mock(ComponentState.class);
        view = mock(ViewDefinitionState.class);
        numberGeneratorService = mock(NumberGeneratorService.class);
        translationService = mock(TranslationService.class);
        when(view.getComponentByReference("number")).thenReturn(numberComponent);
        when(view.getComponentByReference("name")).thenReturn(nameComponent);
    }

    @SuppressWarnings("unchecked")
    private void mockModel() {
        orderGroup = mock(Entity.class);
        order = mock(Entity.class);
        ordersList = mock(EntityList.class);
        Iterator<Entity> ordersListIterator = mock(Iterator.class);
        dataDefinition = mock(DataDefinition.class);

        when(order.getBelongsToField("orderGroup")).thenReturn(orderGroup);
        when(order.getDataDefinition()).thenReturn(dataDefinition);
        when(orderGroup.getDataDefinition()).thenReturn(dataDefinition);
        when(ordersListIterator.hasNext()).thenReturn(true, true, true, false);
        when(ordersListIterator.next()).thenReturn(order, order, order);
        when(ordersList.iterator()).thenReturn(ordersListIterator);
        when(ordersList.size()).thenReturn(3);
        when(ordersList.isEmpty()).thenReturn(false);
        when(orderGroup.getHasManyField("orders")).thenReturn(ordersList);
    }

    @Test
    public final void shouldReturnTrueIfBounaryContainsGivenDate() throws Exception {
        // given
        mockEntityDateRange(orderGroup, 10, 20);
        mockEntityDateRange(order, 11, 19);

        // when
        boolean result = orderGroupsService.checkOrderGroupDateBoundary(orderGroup, ordersList, "", orderGroup);

        // then
        assertTrue(result);
    }

    @Test
    public final void shouldReturnTrueIfLowerBounaryIsNotSetAndUpperContainsEndDate() throws Exception {
        // given
        mockEntityDateRange(orderGroup, 0, 20);
        mockEntityDateRange(order, 1, 19);

        // when
        boolean result = orderGroupsService.checkOrderGroupDateBoundary(orderGroup, ordersList, "", orderGroup);

        // then
        assertTrue(result);
    }

    @Test
    public final void shouldReturnTrueIfUpperBounaryIsNotSetAndLowerContainsStartDate() throws Exception {
        // given
        mockEntityDateRange(orderGroup, 10, 0);
        mockEntityDateRange(order, 11, 30);

        // when
        boolean result = orderGroupsService.checkOrderGroupDateBoundary(orderGroup, ordersList, "", orderGroup);

        // then
        assertTrue(result);
    }

    @Test
    public final void shouldReturnFalseIfUpperBounaryIsExceeded() throws Exception {
        // given
        mockEntityDateRange(orderGroup, 10, 20);
        mockEntityDateRange(order, 11, 30);

        // when
        boolean result = orderGroupsService.checkOrderGroupDateBoundary(orderGroup, ordersList, "", orderGroup);

        // then
        assertFalse(result);
    }

    @Test
    public final void shouldReturnFalseIfLowerBounaryIsExceeded() throws Exception {
        // given
        mockEntityDateRange(orderGroup, 10, 20);
        mockEntityDateRange(order, 9, 19);

        // when
        boolean result = orderGroupsService.checkOrderGroupDateBoundary(orderGroup, ordersList, "", orderGroup);

        // then
        assertFalse(result);
    }

    @Test
    public final void shouldReturnTrueIfBoundariesAreEquals() throws Exception {
        // given
        mockEntityDateRange(orderGroup, 10, 20);
        mockEntityDateRange(order, 10, 20);

        // when
        boolean result = orderGroupsService.checkOrderGroupDateBoundary(orderGroup, ordersList, "", orderGroup);

        // then
        assertTrue(result);
    }

    @Test
    public final void shouldReturnTrueIfBoundariesIsTheSameOneDay() throws Exception {
        // given
        mockEntityDateRange(orderGroup, 10, 10);
        mockEntityDateRange(order, 10, 10);

        // when
        boolean result = orderGroupsService.checkOrderGroupDateBoundary(orderGroup, ordersList, "", orderGroup);

        // then
        assertTrue(result);
    }

    @Test
    public final void shouldReturnFalseIfBounariesAreExceeded() throws Exception {
        // given
        mockEntityDateRange(orderGroup, 10, 20);
        mockEntityDateRange(order, 1, 30);

        // when
        boolean result = orderGroupsService.checkOrderGroupDateBoundary(orderGroup, ordersList, "", orderGroup);

        // then
        assertFalse(result);
    }

    @Test
    public final void shouldReturnTrueIfGroupDateBoundariesAreCorrect() throws Exception {
        // given
        mockEntityDateRange(orderGroup, 10, 20);
        mockEntityDateRange(order, 14, 16);

        // when
        boolean result = orderGroupsService.validateDates(dataDefinition, orderGroup);

        // then
        assertTrue(result);
    }

    @Test
    public final void shouldReturnTrueIfGroupIsNull() throws Exception {
        // when
        boolean result = orderGroupsService.checkOrderGroupDateBoundary(null, ordersList, "", orderGroup);

        // then
        assertTrue(result);
    }

    @Test
    public final void shouldReturnTrueIfOrdersIsEmpty() throws Exception {
        // given
        when(ordersList.size()).thenReturn(0);
        when(ordersList.isEmpty()).thenReturn(true);

        // when
        boolean result = orderGroupsService.checkOrderGroupDateBoundary(orderGroup, ordersList, "", orderGroup);

        // then
        assertTrue(result);
    }

    @Test
    public final void shouldReturnFalseIfGroupDateBoundariesAreIncorrect() throws Exception {
        // given
        mockEntityDateRange(orderGroup, 50, 10);
        mockEntityDateRange(order, 14, 16);

        // when
        boolean result = orderGroupsService.validateDates(dataDefinition, orderGroup);

        // then
        assertFalse(result);
    }

    @Test
    public final void shouldFireValidatorAndReturnFalse() throws Exception {
        // given
        mockEntityDateRange(orderGroup, 10, 20);
        mockEntityDateRange(order, 9, 46);

        // when
        boolean result = orderGroupsService.validateOrderDate(dataDefinition, order);

        // then
        assertFalse(result);
    }

    @Test
    public final void shouldFireValidatorAndReturnTrue() throws Exception {
        // given
        mockEntityDateRange(orderGroup, 10, 40);
        mockEntityDateRange(order, 14, 16);

        // when
        boolean result = orderGroupsService.validateOrderDate(dataDefinition, order);

        // then
        assertTrue(result);
    }

    private void mockEntityDateRange(final Entity entity, final int daysFrom, final int daysTo) {
        when(entity.getField("dateFrom")).thenReturn(getDateWithTimeInterval(daysFrom));
        when(entity.getField("dateTo")).thenReturn(getDateWithTimeInterval(daysTo));
    }

    private Object getDateWithTimeInterval(final int additionalDays) {
        Calendar cal = Calendar.getInstance();
        if (additionalDays == 0) {
            return null;
        } else {
            cal.setTimeInMillis(now);
            cal.add(Calendar.DAY_OF_MONTH, additionalDays);
        }
        return cal.getTime();
    }

    @Test
    public final void shouldNotGenerateNumberAndNameForAlreadyNumberedGroup() throws Exception {
        // given
        when(numberComponent.getFieldValue()).thenReturn("I was been here ...");
        when(nameComponent.getFieldValue()).thenReturn("... and also here :)");

        // when
        orderGroupsService.generateNumberAndName(view);

        // then
        Mockito.verify(numberGeneratorService, Mockito.never()).generateAndInsertNumber(
                (ViewDefinitionState) Mockito.anyObject(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.eq("number"));
        Mockito.verify(nameComponent, Mockito.never()).setFieldValue(Mockito.anyObject());
    }

    @Test
    public final void shouldGenerateNumberAndNameIfDoesNotExists() throws Exception {
        // given
        when(numberComponent.getFieldValue()).thenReturn("");
        when(nameComponent.getFieldValue()).thenReturn("");

        // when
        orderGroupsService.generateNumberAndName(view);

        // then
        Mockito.verify(numberGeneratorService, Mockito.atLeastOnce()).generateAndInsertNumber(
                (ViewDefinitionState) Mockito.anyObject(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.eq("number"));
        Mockito.verify(nameComponent, Mockito.atLeastOnce()).setFieldValue(Mockito.anyObject());
    }

    @Test
    public final void shouldGenerateOnlyNumberIfNameAlreadyExists() throws Exception {
        // given
        when(numberComponent.getFieldValue()).thenReturn("");
        when(nameComponent.getFieldValue()).thenReturn("Qcadoo Framework RLZ!");

        // when
        orderGroupsService.generateNumberAndName(view);

        // then
        Mockito.verify(numberGeneratorService, Mockito.atLeastOnce()).generateAndInsertNumber(
                (ViewDefinitionState) Mockito.anyObject(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.eq("number"));
        Mockito.verify(nameComponent, Mockito.never()).setFieldValue(Mockito.anyObject());
    }
}
