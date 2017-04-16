package com.catfish.validator;

import com.catfish.DefaultValidateCallback;
import com.catfish.ValidateCallback;
import com.catfish.ValidatorElementList;
import com.catfish.annotation.FlowValid;
import com.catfish.annotation.NotThreadSafe;
import com.catfish.cache.Cache;
import com.catfish.cache.CacheManager;
import com.catfish.element.FlowMetaData;
import com.catfish.element.ValidaElement;
import com.catfish.element.WhenElement;
import com.catfish.exception.FlowValidatorRuntimeException;
import com.catfish.exception.UnknownAnnotationException;
import com.catfish.util.Assert;
import com.catfish.util.ReflectUtils;
import org.hibernate.validator.internal.engine.ValidationContext;
import org.hibernate.validator.internal.engine.ValueContext;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintTree;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorManager;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.internal.engine.time.DefaultTimeProvider;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.util.TypeResolutionHelper;
import org.hibernate.validator.internal.util.privilegedactions.GetAnnotationParameter;
import org.hibernate.validator.spi.time.TimeProvider;
import org.hibernate.validator.spi.valuehandling.ValidatedValueUnwrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import javax.validation.*;
import javax.validation.groups.Default;
import javax.validation.metadata.ConstraintDescriptor;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;

/**
 * Created by apple on 17/4/6.
 */
@NotThreadSafe
public class FlowValidator {

    private ConstraintValidatorManager constraintValidatorManager;
    private MessageInterpolator messageInterpolator;
    private ConstraintValidatorFactory constraintValidatorFactory;
    private TraversableResolver traversableResolver;
    private TimeProvider timeProvider;
    private List<ValidatedValueUnwrapper<?>> validatedValueHandlers;
    private ValidatorFactory validatorFactory;
    private Boolean isFailFast = false;

    private BindingResult bindingResult = new BeanPropertyBindingResult(null, "valida");
    private ValidateCallback callback = new DefaultValidateCallback();

    private Logger logger = LoggerFactory.getLogger(getClass());

    private FlowMetaData flowMetaData = new FlowMetaData();

    //用来记录when
    private Map<Integer, ValidatorElementList<WhenElement>> elementListMap = new HashMap<>();

    //记录最后一个when的地方
    private int lastWhen = -1;

    private final String FAIL_FAST = "hibernate.validator.fail_fast";

    private FlowValidator() {
    }

    public FlowValidator(ValidatorFactory validatorFactory) {
        this.validatorFactory = validatorFactory;
        init();
    }

    private void init() {
        Assert.notNull(validatorFactory);
        messageInterpolator = validatorFactory.getMessageInterpolator();
        constraintValidatorFactory = validatorFactory.getConstraintValidatorFactory();
        traversableResolver = validatorFactory.getTraversableResolver();
        timeProvider = DefaultTimeProvider.getInstance();
        LocalValidatorFactoryBean localValidatorFactoryBean = (LocalValidatorFactoryBean) validatorFactory;
        Map<String, String> validationPropertyMap = localValidatorFactoryBean.getValidationPropertyMap();
        if (validationPropertyMap.containsKey(FAIL_FAST)) {
            isFailFast = Boolean.valueOf(validationPropertyMap.get(FAIL_FAST));
        }
        validatedValueHandlers = new ArrayList<>();
        constraintValidatorManager = new ConstraintValidatorManager(validatorFactory.getConstraintValidatorFactory());
    }

    public static FlowValidator check(ValidatorFactory validatorFactory) {
        return new FlowValidator(validatorFactory);
    }

    private void validaBean(ValueContext t, ValidationContext validationContext, Class<?>... groups) {
        Validator validator = validatorFactory.getValidator();
        Set<ConstraintViolation<Object>> violations = validator.validate(t.getCurrentValidatedValue(), groups);
        validationContext.addConstraintFailures(violations);
    }

    public <T> FlowValidator on(Class<? extends Annotation> annotation, T values) {
        on(annotation, new Class[]{Default.class}, "", values);
        return this;
    }

    public <T> FlowValidator on(Class<? extends Annotation> annotation, String propertyPath, T values) {
        on(annotation, new Class[]{Default.class}, propertyPath, values);
        return this;
    }

    public <T> FlowValidator on(Class<? extends Annotation> annotation, Class<?>[] groups, T values) {
        on(annotation, groups, "", values);
        return this;
    }

    public <T> FlowValidator on(Class<? extends Annotation> annotation, Class<?>[] group, String propertyPath, T values) {
        if (elementListMap.isEmpty()) {
            when(true);
        }
        ValidatorElementList<WhenElement> whenElementValidatorElementList = elementListMap.get(lastWhen);
        ValidatorElementList<ValidaElement> validaElementValidatorElementList = whenElementValidatorElementList.getElement(lastWhen).getValidatorElements();
        int index = whenElementValidatorElementList.size() - 1;
        whenElementValidatorElementList.getElement(index).getValidatorElements();
        validaElementValidatorElementList.add(new ValidaElement(propertyPath, values, annotation, group));
        flowMetaData.setWhenElementList(whenElementValidatorElementList);
        return this;
    }

    /**
     * 获取注解对应annotation
     *
     * @param annotationType
     * @return
     */
    private Annotation getAnnotation(String className, Method[] methods, Class<? extends Annotation> annotationType, Class group) {
        Annotation an = null;
        String cacheKey = getKey(className, annotationType.getSimpleName(), group.getSimpleName());
            if (CacheManager.getCacheInfo(cacheKey)!=null){
                logger.debug("user cache");
                return (Annotation) CacheManager.getCacheInfo(cacheKey).getValue();
            }
        for (Method method : methods
                ) {
            Annotation[] annotations = method.getAnnotationsByType(annotationType);
            for (Annotation annotation : annotations) {
                if (annotation instanceof Valid) {
                    return annotation;
                }
                final Class<?>[] groupsFromAnnotation = run(
                        GetAnnotationParameter.action(annotation, ConstraintHelper.GROUPS, Class[].class)
                );
                if (groupsFromAnnotation.length == 0) {
                    if (Default.class.isAssignableFrom(group)) {
                                CacheManager.putCache(cacheKey, new Cache(cacheKey,annotation,1000,false));
                                return annotation;
                            }
                    } else {
                        for (Class clazz : groupsFromAnnotation
                                ) {
                                CacheManager.putCache(cacheKey, new Cache(cacheKey,annotation,1000,false));
                                if (clazz.isAssignableFrom(group)) {
                                    return annotation;
                                }
                        }
                    }

            }
        }

        if (an == null) {
            throw new UnknownAnnotationException("找不到" + annotationType + "(" + Arrays.asList(group) + ")");
        }
        return an;
    }


    public FlowValidator when(boolean expression) {
        WhenElement whenElement = new WhenElement();
        whenElement.setExpression(expression);
        ValidatorElementList<WhenElement> validatorElementList = flowMetaData.getWhenElementList();
        validatorElementList.add(whenElement);
        lastWhen++;
        elementListMap.put(lastWhen, validatorElementList);
        return this;
    }


    public BindingResult end() {
        return bindingResult;
    }

    public  FlowValidator valida(ValidateCallback callback) {
        ValidatorElementList<WhenElement> whenElements = flowMetaData.getWhenElementList();
        if (whenElements.isEmpty()) {
            logger.debug("valida isEmpty");
            return this;
        }
        long start = System.currentTimeMillis();
        String className = ReflectUtils.getCaller();
        Class flowValidClass = getFlowValidClass(className);
        Method[] methods = getFlowValidMethods(className);
        Iterator<WhenElement> iterator = whenElements.createIterator();
        while (iterator.hasNext()) {
            WhenElement whenElement = iterator.next();
            if (!whenElement.getExpression()) {
                continue;
            }
            ValidatorElementList<ValidaElement> validatorElementList = whenElement.getValidatorElements();
            Iterator<ValidaElement> iterator1 = validatorElementList.createIterator();
            while (iterator1.hasNext()) {
                ValidaElement validaElement = iterator1.next();
                ConstraintHelper constraintHelper = new ConstraintHelper();
                ValidationContext validationContext = getValidationContext().forValidateValue(Object.class);
                String flowValidClassName=flowValidClass.getName();
                Class<? extends Annotation> annotationType = validaElement.getAnnotation();
                Object value = validaElement.getValues();
                ValueContext valueContext = ValueContext.getLocalExecutionContext(Object.class, null,
                        PathImpl.createPathFromString(validaElement.getPropertyPath()));
                Class declaringClass = Object.class;
                if (value != null) {
                    declaringClass = value.getClass();
                }
                valueContext.setDeclaredTypeOfValidatedElement(ConstraintLocation.forClass(declaringClass).getTypeForValidatorResolution());
                valueContext.setCurrentValidatedValue(value);
                //校验bean
                if (annotationType.isAssignableFrom(Valid.class)) {
                    validaBean(valueContext, validationContext, validaElement.getGroups());
                } else {
                    Annotation an = getAnnotation(flowValidClassName, methods, annotationType, validaElement.getGroups()[0]);
                    ConstraintDescriptorImpl<? extends Annotation> constraintDescriptor = buildConstraintDescriptor(
                            null, an, ElementType.TYPE, constraintHelper);
                    valueContext.setElementType(constraintDescriptor.getElementType());
                    validaGeneric(validationContext, constraintDescriptor, valueContext);
                }
                Set<ConstraintViolation> fails = validationContext.getFailingConstraints();
                Iterator<ConstraintViolation> constraintViolationIterator = fails.iterator();
                while (constraintViolationIterator.hasNext()) {
                    ConstraintViolation constraintViolation = constraintViolationIterator.next();
                    StringBuffer buf = new StringBuffer();
                    String message = constraintViolation.getMessage();
                    buf.append(message);
                    ObjectError error = new ObjectError(constraintViolation.getPropertyPath().toString(), buf.toString());
                    bindingResult.addError(error);
                }
                if (validationContext.isFailFastModeEnabled()) {
                    break;
                }
            }
        }
        if (bindingResult.hasErrors()) {
            callback.onFail(whenElements, bindingResult);
        } else {
            callback.onSuccess(whenElements);
        }
        int timeElapsed = (int) (System.currentTimeMillis() - start);
        logger.debug("耗时:" + timeElapsed);
        return this;
    }

    public  FlowValidator valida() {
        return valida(callback);
    }



    private  void validaGeneric(ValidationContext validationContext,
                                                         ConstraintDescriptor constraintDescriptor,
                                                         ValueContext valueContext) {
        ConstraintTree constraintTree = new ConstraintTree((ConstraintDescriptorImpl) constraintDescriptor);
        constraintTree.validateConstraints(validationContext, valueContext);
    }


    private  ConstraintTree createConstraintTree(ConstraintDescriptorImpl composingDescriptor) {
        return new ConstraintTree(composingDescriptor);
    }

    private ValidationContext.ValidationContextBuilder getValidationContext() {
        return ValidationContext.getValidationContext(
                constraintValidatorManager,
                messageInterpolator,
                constraintValidatorFactory,
                traversableResolver,
                timeProvider,
                validatedValueHandlers,
                new TypeResolutionHelper(),
                isFailFast
        );
    }


    private <A extends Annotation> ConstraintDescriptorImpl<A> buildConstraintDescriptor(Member member,
                                                                                         A annotation,
                                                                                         ElementType type,
                                                                                         ConstraintHelper constraintHelper) {
        return new ConstraintDescriptorImpl<A>(
                constraintHelper,
                member,
                annotation,
                type
        );
    }



    private <P> P run(PrivilegedAction<P> action) {
        return System.getSecurityManager() != null ? AccessController.doPrivileged(action) : action.run();
    }

    private Class<?> getFlowValidClass(String className) {
        FlowValid flowValid = null;
        try {
            flowValid = Class.forName(className).getAnnotation(FlowValid.class);
        } catch (ClassNotFoundException e) {
            throw new FlowValidatorRuntimeException("没有该方法:" + className);
        }
        if (flowValid == null) {
            throw new FlowValidatorRuntimeException("没有找到FlowValid注解");
        }
        return flowValid.value();
    }

    private Method[] getFlowValidMethods(String className) {
        return getFlowValidClass(className).getMethods();
    }

    /**
     * 缓存key生成
     *
     * @return
     */
    private String getKey(String className, String annotationName, String group) {
        return className + ":" + annotationName + ":" + group;
    }

}
