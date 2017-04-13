package com.catfish.validator;

import com.catfish.DefaultValidateCallback;
import com.catfish.ValidateCallback;
import com.catfish.ValidatorElementList;
import com.catfish.annotation.FlowValid;
import com.catfish.annotation.NotThreadSafe;
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
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorManager;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintViolationCreationContext;
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
import javax.validation.constraintvalidation.SupportedValidationTarget;
import javax.validation.constraintvalidation.ValidationTarget;
import javax.validation.groups.Default;
import javax.validation.metadata.ConstraintDescriptor;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;

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

    public FlowValidator on(Class<? extends Annotation> annotation, Object values) {
        on(annotation, new Class[]{Default.class}, "", values);
        return this;
    }

    public FlowValidator on(Class<? extends Annotation> annotation, String name, Object values) {
        on(annotation, new Class[]{Default.class}, name, values);
        return this;
    }

    public FlowValidator on(Class<? extends Annotation> annotation, Class<?>[] groups, Object values) {
        on(annotation, groups, "", values);
        return this;
    }

    public FlowValidator on(Class<? extends Annotation> annotation, Class<?>[] group, String name, Object values) {
        if (elementListMap.isEmpty()) {
            when(true);
        }
        ValidatorElementList<WhenElement> whenElementValidatorElementList = elementListMap.get(lastWhen);
        ValidatorElementList<ValidaElement> validaElementValidatorElementList = whenElementValidatorElementList.getElement(lastWhen).getValidatorElements();
        int index = whenElementValidatorElementList.size() - 1;
        whenElementValidatorElementList.getElement(index).getValidatorElements();
        validaElementValidatorElementList.add(new ValidaElement(name, values, annotation, group));
        flowMetaData.setWhenElementList(whenElementValidatorElementList);
        return this;
    }

    /**
     * 获取注解对应annotation
     *
     * @param annotationType
     * @param groups
     * @return
     */
    private Annotation getAnnotation(Method[] methods, Class<? extends Annotation> annotationType, Class[] groups) {
        Annotation an = null;
        for (Method method : methods
                ) {
            Annotation[] annotations = method.getAnnotations();
            for (Annotation annotation : annotations) {
                if (annotation.annotationType().isAssignableFrom(annotationType)) {
                    if (annotation instanceof Valid) {
                        return annotation;
                    }
                    final Class<?>[] groupsFromAnnotation = run(
                            GetAnnotationParameter.action(annotation, ConstraintHelper.GROUPS, Class[].class)
                    );
                    if (groupsFromAnnotation.length == 0) {
                        for (Class group :
                                groups) {
                            if (Default.class.isAssignableFrom(group)) {
                                return annotation;
                            }
                        }
                    } else {
                        for (Class clazz : groupsFromAnnotation
                                ) {
                            for (Class group :
                                    groups) {
                                if (clazz.isAssignableFrom(group)) {
                                    return annotation;
                                }
                            }
                        }
                    }

                }
            }
        }

        if (an == null) {
            throw new UnknownAnnotationException("找不到" + annotationType + "(" + Arrays.asList(groups) + ")");
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

    public FlowValidator valida(ValidateCallback callback) {
        ValidatorElementList<WhenElement> whenElements = flowMetaData.getWhenElementList();
        if (whenElements.isEmpty()) {
            logger.debug("valida isEmpty");
            return this;
        }
        long start = System.currentTimeMillis();
        String className = ReflectUtils.getCaller();
        Method[] methods = getMethods(className);
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
                Annotation an = getAnnotation(methods, validaElement.getAnnotation(), validaElement.getGroups());
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
                if (an instanceof Valid) {
                    validaBean(valueContext, validationContext, validaElement.getGroups());
                } else {
                    ConstraintDescriptorImpl<? extends Annotation> constraintDescriptor = buildConstraintDescriptor(
                            null, an, ElementType.TYPE, constraintHelper);
                    List<Class<? extends ConstraintValidator>> constraintValidators =
                            findValidatorClasses(validaElement.getAnnotation(), ValidationTarget.ANNOTATED_ELEMENT, constraintHelper);
                    valueContext.setElementType(constraintDescriptor.getElementType());
                    //基本校验类型
                    if (constraintValidators.size() > 0) {
                        validaGeneric(validationContext, constraintDescriptor, constraintValidators, valueContext);
                    } else {
                        //复合校验类型
                        validaCompose(validationContext, constraintDescriptor, valueContext);
                    }
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
//                iterator1.remove();
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
        //还原参数
//        lastWhen=-1;
//        elementListMap.clear();
        int timeElapsed = (int) (System.currentTimeMillis() - start);
        logger.debug("耗时:" + timeElapsed);
        return this;
    }

    public FlowValidator valida() {
        return valida(callback);
    }


    private void validaCompose(ValidationContext validationContext, ConstraintDescriptorImpl constraintDescriptor, ValueContext valueContext) {
        Set<ConstraintDescriptorImpl<?>> composingConstraints = constraintDescriptor.getComposingConstraintImpls();
        for (ConstraintDescriptorImpl<?> composingDescriptor : composingConstraints) {
            ConstraintTree<?> treeNode = createConstraintTree(composingDescriptor);
            treeNode.validateConstraints(validationContext, valueContext);
        }
    }

    private void validaGeneric(ValidationContext validationContext,
                               ConstraintDescriptor constraintDescriptor,
                               List<Class<? extends ConstraintValidator>> constraintValidators, ValueContext valueContext) {
        ConstraintValidatorContext constraintValidatorContext = new ConstraintValidatorContextImpl(
                validationContext.getParameterNames(),
                validationContext.getTimeProvider(),
                valueContext.getPropertyPath(),
                constraintDescriptor
        );
        for (Class<? extends ConstraintValidator> c : constraintValidators
                ) {
            ConstraintValidator constraintValidator = validatorFactory.getConstraintValidatorFactory().getInstance(
                    c
            );
            boolean isValid;
            try {
                constraintValidator.initialize(constraintDescriptor.getAnnotation());
                isValid = constraintValidator.isValid(valueContext.getCurrentValidatedValue(), constraintValidatorContext);
            } catch (RuntimeException e) {
                continue;
            }
            if (!isValid) {
                Set<ConstraintViolation> set = new HashSet();
                String message = constraintValidatorContext.getDefaultConstraintMessageTemplate();
                ConstraintViolation violation = validationContext.createConstraintViolation(
                        valueContext, new ConstraintViolationCreationContext(message, valueContext.getPropertyPath()), constraintDescriptor
                );
                set.add(violation);
                validationContext.addConstraintFailures(set);
            }
        }
    }


    private <U extends Annotation> ConstraintTree<U> createConstraintTree(ConstraintDescriptorImpl<U> composingDescriptor) {
        return new ConstraintTree<U>(composingDescriptor);
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

    public <A extends Annotation> List<Class<? extends ConstraintValidator>> findValidatorClasses(
            Class<A> annotationType, ValidationTarget validationTarget, ConstraintHelper constraintHelper) {
        List<Class<? extends ConstraintValidator<A, ?>>> validatorClasses = constraintHelper.getAllValidatorClasses(annotationType);
        List<Class<? extends ConstraintValidator>> matchingValidatorClasses = newArrayList();

        for (Class<? extends ConstraintValidator<A, ?>> validatorClass : validatorClasses) {
            if (supportsValidationTarget(validatorClass, validationTarget)) {
                matchingValidatorClasses.add(validatorClass);
            }
        }

        return matchingValidatorClasses;
    }

    private boolean supportsValidationTarget(Class<? extends ConstraintValidator<?, ?>> validatorClass, ValidationTarget target) {
        SupportedValidationTarget supportedTargetAnnotation = validatorClass.getAnnotation(
                SupportedValidationTarget.class
        );

        //by default constraints target the annotated element
        if (supportedTargetAnnotation == null) {
            return target == ValidationTarget.ANNOTATED_ELEMENT;
        }

        return Arrays.asList(supportedTargetAnnotation.value()).contains(target);
    }


    private <P> P run(PrivilegedAction<P> action) {
        return System.getSecurityManager() != null ? AccessController.doPrivileged(action) : action.run();
    }

    private Method[] getMethods(String className) {
        FlowValid flowValid = null;
        try {
            flowValid = Class.forName(className).getAnnotation(FlowValid.class);
        } catch (ClassNotFoundException e) {
            throw new FlowValidatorRuntimeException("没有该方法:" + className);
        }
        if (flowValid == null) {
            throw new FlowValidatorRuntimeException("没有找到FlowValid注解");
        }
        return flowValid.value().getMethods();
    }

}
