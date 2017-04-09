package com.catfish.validator;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.internal.engine.ValidationContext;
import org.hibernate.validator.internal.engine.ValidatorFactoryImpl;
import org.hibernate.validator.internal.engine.ValueContext;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintTree;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorManager;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.internal.engine.time.DefaultTimeProvider;
import org.hibernate.validator.internal.engine.valuehandling.UnwrapMode;
import org.hibernate.validator.internal.metadata.aggregated.ParameterMetaData;
import org.hibernate.validator.internal.metadata.aggregated.ValidatableParametersMetaData;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.metadata.facets.Cascadable;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedParameter;
import org.hibernate.validator.internal.util.TypeResolutionHelper;
import org.hibernate.validator.spi.time.TimeProvider;
import org.hibernate.validator.spi.valuehandling.ValidatedValueUnwrapper;

import javax.validation.*;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.Member;
import java.util.*;

/**
 * Created by apple on 17/4/6.
 */
public class FlowValidator {

    public static <T> void doValid(T t) {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<T>> violations = validator.validate(t);
        if (violations.size() > 0) {
            StringBuffer buf = new StringBuffer();
            ResourceBundle bundle = ResourceBundle.getBundle("messages");
            for (ConstraintViolation<T> violation : violations) {
                buf.append("-" + bundle.getString(violation.getPropertyPath().toString()));
                buf.append(violation.getMessage() + "<BR>\n");
            }
        }
        return;
    }


    ConstraintValidatorManager constraintValidatorManager;
    MessageInterpolator messageInterpolator;
    ConstraintValidatorFactory constraintValidatorFactory;
    TraversableResolver traversableResolver;
    TimeProvider timeProvider;
    List<ValidatedValueUnwrapper<?>> validatedValueHandlers;

    public void valid() {
        ValidatorFactory validatorFactory = Validation.byProvider(HibernateValidator.class)
                .configure()
                .failFast(true)
//                .constraintValidatorFactory()

                .buildValidatorFactory();
//        ValidatorContext vc = validatorFactory.usingContext();
        messageInterpolator = validatorFactory.getMessageInterpolator();
        constraintValidatorFactory = validatorFactory.getConstraintValidatorFactory();
        traversableResolver = validatorFactory.getTraversableResolver();
        timeProvider = DefaultTimeProvider.getInstance();
        ValidatorFactoryImpl validatorFactoryImpl = (ValidatorFactoryImpl) validatorFactory;
        validatedValueHandlers = validatorFactoryImpl.getValidatedValueHandlers();

//        ValidatorContextImpl vci = (ValidatorContextImpl) vc;

        constraintValidatorManager = new ConstraintValidatorManager(validatorFactory.getConstraintValidatorFactory());
//        Validator validator = validatorFactory.getValidator();
//        BeanDescriptor beanDescriptor = validator.getConstraintsForClass(String.class);
        ConstraintHelper constraintHelper = new ConstraintHelper();
        ValidationContext validationContext = getValidationContext().forValidateValue(Object.class);
        Annotation an = null;
        try {
            an = Class.forName("com.catfish.validator.FlowValidator").getMethod("aaaa").getAnnotation(NotEmpty.class);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        /*Annotation annotation= AnnotationParser.annotationForMap
                (NotEmpty.class, null);*/

        ConstraintDescriptorImpl<? extends Annotation> constraintDescriptor = buildConstraintDescriptor(null, an, ElementType.TYPE, constraintHelper);

         /*  ConstraintValidatorContextImpl constraintValidatorContext = new ConstraintValidatorContextImpl(
                validationContext.getParameterNames(),
                validationContext.getTimeProvider(),
                null,
                constraintDescriptor
        );

     List<Class<? extends ConstraintValidator<NotEmpty, ?>>> constraintValidators = constraintHelper.findValidatorClasses(NotEmpty.class, ValidationTarget.ANNOTATED_ELEMENT);

        for (Class<? extends ConstraintValidator<NotEmpty, ?>> c : constraintValidators
                ) {
            ConstraintValidator constraintValidator = (ConstraintValidator) validatorFactory.getConstraintValidatorFactory().getInstance(
                    c
            );
            System.out.println(constraintValidator.isValid("", constraintValidatorContext));
            System.out.println(constraintValidatorContext.getDefaultConstraintMessageTemplate());
//            constraintValidator.isValid()
        }*/
        ConstrainedParameter constrainedParameter = new ConstrainedParameter(
                ConfigurationSource.ANNOTATION,
                ConstraintLocation.forClass(Object.class),
                Object.class.getGenericSuperclass(),
                0,
                "arg0",
                new HashSet<MetaConstraint<?>>(),
                new HashSet<MetaConstraint<?>>(),
                new HashMap<Class<?>, Class<?>>(),
                false,
                UnwrapMode.AUTOMATIC
        );

        Cascadable parameterMetaData = new ParameterMetaData.Builder(Object.class, constrainedParameter, constraintHelper).build();
        List<Cascadable> list = new ArrayList<Cascadable>();
        list.add(parameterMetaData);
        ValidatableParametersMetaData validatableParametersMetaData = new ValidatableParametersMetaData(list);

        ValueContext valueContext = ValueContext.getLocalExecutionContext("11", validatableParametersMetaData, PathImpl.createRootPath());
        valueContext.setElementType(constraintDescriptor.getElementType());

        valueContext.setDeclaredTypeOfValidatedElement(ConstraintLocation.forClass(String.class).getTypeForValidatorResolution());
        valueContext.setCurrentValidatedValue("");
        Set<ConstraintDescriptorImpl<?>> composingConstraints = constraintDescriptor.getComposingConstraintImpls();
        for (ConstraintDescriptorImpl<?> composingDescriptor : composingConstraints) {
            ConstraintTree<?> treeNode = createConstraintTree(composingDescriptor);
            treeNode.validateConstraints(validationContext, valueContext);
        }
        Set<ConstraintViolation> fail = validationContext.getFailingConstraints();
        Iterator<ConstraintViolation> iterator = fail.iterator();
        while (iterator.hasNext()) {
            ConstraintViolation constraintViolation= iterator.next();
            System.out.println(constraintViolation.getMessageTemplate());
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
                false
        );
    }

    @NotEmpty
    public void aaaa() {

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

}
