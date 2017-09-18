/* This file was generated by SableCC (http://www.sablecc.org/). */

package com.abstratt.mdd.frontend.textuml.grammar.analysis;

import com.abstratt.mdd.frontend.textuml.grammar.node.*;

public interface Analysis extends Switch
{
    Object getIn(Node node);
    void setIn(Node node, Object o);
    Object getOut(Node node);
    void setOut(Node node, Object o);

    void caseStart(Start node);
    void caseAStart(AStart node);
    void caseAPackageHeading(APackageHeading node);
    void caseAModelPackageType(AModelPackageType node);
    void caseAProfilePackageType(AProfilePackageType node);
    void caseAPackagePackageType(APackagePackageType node);
    void caseAQualifiedIdentifierList(AQualifiedIdentifierList node);
    void caseAQualifiedIdentifierListTail(AQualifiedIdentifierListTail node);
    void caseAQualifiedIdentifier(AQualifiedIdentifier node);
    void caseAForcefullyQualifiedIdentifier(AForcefullyQualifiedIdentifier node);
    void caseAQualifiedIdentifierBase(AQualifiedIdentifierBase node);
    void caseAQualifiedIdentifierTail(AQualifiedIdentifierTail node);
    void caseAGlobalDirectiveSection(AGlobalDirectiveSection node);
    void caseALoadGlobalDirective(ALoadGlobalDirective node);
    void caseAApplyGlobalDirective(AApplyGlobalDirective node);
    void caseAImportGlobalDirective(AImportGlobalDirective node);
    void caseALoadDecl(ALoadDecl node);
    void caseAApplyProfileDecl(AApplyProfileDecl node);
    void caseAImportDecl(AImportDecl node);
    void caseAOptionalAlias(AOptionalAlias node);
    void caseAEmptyOptionalAlias(AEmptyOptionalAlias node);
    void caseANamespaceContents(ANamespaceContents node);
    void caseASubNamespace(ASubNamespace node);
    void caseATopLevelElement(ATopLevelElement node);
    void caseAClassTopLevelElementChoice(AClassTopLevelElementChoice node);
    void caseAAssociationTopLevelElementChoice(AAssociationTopLevelElementChoice node);
    void caseAStereotypeTopLevelElementChoice(AStereotypeTopLevelElementChoice node);
    void caseAOperationTopLevelElementChoice(AOperationTopLevelElementChoice node);
    void caseAFunctionTopLevelElementChoice(AFunctionTopLevelElementChoice node);
    void caseAPrimitiveTopLevelElementChoice(APrimitiveTopLevelElementChoice node);
    void caseASubnamespaceTopLevelElementChoice(ASubnamespaceTopLevelElementChoice node);
    void caseAMinimalTypeIdentifier(AMinimalTypeIdentifier node);
    void caseAQualifiedSingleTypeIdentifier(AQualifiedSingleTypeIdentifier node);
    void caseAAnySingleTypeIdentifier(AAnySingleTypeIdentifier node);
    void caseATupleTypeSingleTypeIdentifier(ATupleTypeSingleTypeIdentifier node);
    void caseAMinimalTypeIdentifierList(AMinimalTypeIdentifierList node);
    void caseAMinimalTypeIdentifierListTail(AMinimalTypeIdentifierListTail node);
    void caseATypeIdentifier(ATypeIdentifier node);
    void caseAFunctionTypeIdentifier(AFunctionTypeIdentifier node);
    void caseAOptionalMultiplicity(AOptionalMultiplicity node);
    void caseASingleMultiplicitySpec(ASingleMultiplicitySpec node);
    void caseAIntervalMultiplicitySpec(AIntervalMultiplicitySpec node);
    void caseAMultiplicityConstraints(AMultiplicityConstraints node);
    void caseAMultiplicityConstraintList(AMultiplicityConstraintList node);
    void caseAMultiplicityConstraintTail(AMultiplicityConstraintTail node);
    void caseAOrderedMultiplicityConstraint(AOrderedMultiplicityConstraint node);
    void caseAUnorderedMultiplicityConstraint(AUnorderedMultiplicityConstraint node);
    void caseAUniqueMultiplicityConstraint(AUniqueMultiplicityConstraint node);
    void caseANonuniqueMultiplicityConstraint(ANonuniqueMultiplicityConstraint node);
    void caseAOptionalFormalTemplateParameters(AOptionalFormalTemplateParameters node);
    void caseAFormalTemplateParameterList(AFormalTemplateParameterList node);
    void caseAFormalTemplateParameterTail(AFormalTemplateParameterTail node);
    void caseAFormalTemplateParameter(AFormalTemplateParameter node);
    void caseATemplateBinding(ATemplateBinding node);
    void caseATemplateParameterList(ATemplateParameterList node);
    void caseATemplateParameterTail(ATemplateParameterTail node);
    void caseATemplateParameter(ATemplateParameter node);
    void caseAAssociationDef(AAssociationDef node);
    void caseAAssociationHeader(AAssociationHeader node);
    void caseAAssociationAssociationKind(AAssociationAssociationKind node);
    void caseAAggregationAssociationKind(AAggregationAssociationKind node);
    void caseACompositionAssociationKind(ACompositionAssociationKind node);
    void caseAAssociationRoleDeclList(AAssociationRoleDeclList node);
    void caseAEmptyAssociationRoleDeclList(AEmptyAssociationRoleDeclList node);
    void caseAAssociationRoleDecl(AAssociationRoleDecl node);
    void caseAOwnedAssociationEnd(AOwnedAssociationEnd node);
    void caseAMemberAssociationEnd(AMemberAssociationEnd node);
    void caseAAssociationOwnedEnd(AAssociationOwnedEnd node);
    void caseAAssociationMemberEnd(AAssociationMemberEnd node);
    void caseAAssociationModifiers(AAssociationModifiers node);
    void caseAAssociationModifierList(AAssociationModifierList node);
    void caseAEmptyAssociationModifierList(AEmptyAssociationModifierList node);
    void caseANavigableAssociationModifier(ANavigableAssociationModifier node);
    void caseAReadonlyAssociationModifier(AReadonlyAssociationModifier node);
    void caseAClassDef(AClassDef node);
    void caseAClassHeader(AClassHeader node);
    void caseAClassModifiers(AClassModifiers node);
    void caseAClassModifierList(AClassModifierList node);
    void caseAEmptyClassModifierList(AEmptyClassModifierList node);
    void caseAVisibilityClassModifier(AVisibilityClassModifier node);
    void caseAAbstractClassModifier(AAbstractClassModifier node);
    void caseAExternalClassModifier(AExternalClassModifier node);
    void caseARoleClassModifier(ARoleClassModifier node);
    void caseAClassClassType(AClassClassType node);
    void caseAInterfaceClassType(AInterfaceClassType node);
    void caseADatatypeClassType(ADatatypeClassType node);
    void caseAActorClassType(AActorClassType node);
    void caseASignalClassType(ASignalClassType node);
    void caseAComponentClassType(AComponentClassType node);
    void caseAEnumerationClassType(AEnumerationClassType node);
    void caseAClassImplementsSection(AClassImplementsSection node);
    void caseAEmptyClassImplementsSection(AEmptyClassImplementsSection node);
    void caseAClassImplementsList(AClassImplementsList node);
    void caseAClassImplementsListTail(AClassImplementsListTail node);
    void caseAClassImplementsItem(AClassImplementsItem node);
    void caseAClassSpecializesSection(AClassSpecializesSection node);
    void caseAEmptyClassSpecializesSection(AEmptyClassSpecializesSection node);
    void caseAClassSpecializesList(AClassSpecializesList node);
    void caseAClassSpecializesItem(AClassSpecializesItem node);
    void caseAClassSpecializesListTail(AClassSpecializesListTail node);
    void caseAFeatureDeclList(AFeatureDeclList node);
    void caseAEmptyFeatureDeclList(AEmptyFeatureDeclList node);
    void caseAFeatureDecl(AFeatureDecl node);
    void caseAModifiers(AModifiers node);
    void caseAModifierList(AModifierList node);
    void caseAEmptyModifierList(AEmptyModifierList node);
    void caseAVisibilityModifier(AVisibilityModifier node);
    void caseAStaticModifier(AStaticModifier node);
    void caseAAbstractModifier(AAbstractModifier node);
    void caseADerivedModifier(ADerivedModifier node);
    void caseAReadonlyModifier(AReadonlyModifier node);
    void caseAIdModifier(AIdModifier node);
    void caseAPublicVisibilityModifier(APublicVisibilityModifier node);
    void caseAPrivateVisibilityModifier(APrivateVisibilityModifier node);
    void caseAPackageVisibilityModifier(APackageVisibilityModifier node);
    void caseAProtectedVisibilityModifier(AProtectedVisibilityModifier node);
    void caseAStateMachineFeatureType(AStateMachineFeatureType node);
    void caseAOperationFeatureType(AOperationFeatureType node);
    void caseAAttributeFeatureType(AAttributeFeatureType node);
    void caseALiteralFeatureType(ALiteralFeatureType node);
    void caseAReferenceFeatureType(AReferenceFeatureType node);
    void caseAReceptionFeatureType(AReceptionFeatureType node);
    void caseAPortFeatureType(APortFeatureType node);
    void caseADependencyFeatureType(ADependencyFeatureType node);
    void caseAInvariantFeatureType(AInvariantFeatureType node);
    void caseAConnectorFeatureType(AConnectorFeatureType node);
    void caseAStateMachineDecl(AStateMachineDecl node);
    void caseAStateDecl(AStateDecl node);
    void caseAStateModifierList(AStateModifierList node);
    void caseAInitialStateModifier(AInitialStateModifier node);
    void caseATerminateStateModifier(ATerminateStateModifier node);
    void caseAFinalStateModifier(AFinalStateModifier node);
    void caseAStateBehavior(AStateBehavior node);
    void caseADoStateBehaviorModifier(ADoStateBehaviorModifier node);
    void caseAEntryStateBehaviorModifier(AEntryStateBehaviorModifier node);
    void caseAExitStateBehaviorModifier(AExitStateBehaviorModifier node);
    void caseANameStateBehaviorDefinition(ANameStateBehaviorDefinition node);
    void caseABehaviorStateBehaviorDefinition(ABehaviorStateBehaviorDefinition node);
    void caseATransitionDecl(ATransitionDecl node);
    void caseATransitionGuard(ATransitionGuard node);
    void caseATransitionEffect(ATransitionEffect node);
    void caseATransitionTriggers(ATransitionTriggers node);
    void caseATransitionTriggerTail(ATransitionTriggerTail node);
    void caseAEmptyTransitionTriggerTail(AEmptyTransitionTriggerTail node);
    void caseACallTransitionTrigger(ACallTransitionTrigger node);
    void caseASignalTransitionTrigger(ASignalTransitionTrigger node);
    void caseAAnyTransitionTrigger(AAnyTransitionTrigger node);
    void caseAReceptionDecl(AReceptionDecl node);
    void caseAOperationDecl(AOperationDecl node);
    void caseAOperationConstraint(AOperationConstraint node);
    void caseAPreconditionOperationConstraintKernel(APreconditionOperationConstraintKernel node);
    void caseAPermissionOperationConstraintKernel(APermissionOperationConstraintKernel node);
    void caseAOperationPrecondition(AOperationPrecondition node);
    void caseAPermissionConstraint(APermissionConstraint node);
    void caseAPermissionExpression(APermissionExpression node);
    void caseAPermissionRoles(APermissionRoles node);
    void caseAEmptyPermissionRoles(AEmptyPermissionRoles node);
    void caseAAccessCapabilities(AAccessCapabilities node);
    void caseAAllAccessCapabilities(AAllAccessCapabilities node);
    void caseANoneAccessCapabilities(ANoneAccessCapabilities node);
    void caseAEmptyAccessCapabilities(AEmptyAccessCapabilities node);
    void caseAAccessCapabilityList(AAccessCapabilityList node);
    void caseAAccessCapabilityTail(AAccessCapabilityTail node);
    void caseAReadAccessCapability(AReadAccessCapability node);
    void caseACreateAccessCapability(ACreateAccessCapability node);
    void caseAUpdateAccessCapability(AUpdateAccessCapability node);
    void caseADeleteAccessCapability(ADeleteAccessCapability node);
    void caseAStaticCallAccessCapability(AStaticCallAccessCapability node);
    void caseAInstanceCallAccessCapability(AInstanceCallAccessCapability node);
    void caseAExtentAccessCapability(AExtentAccessCapability node);
    void caseAPreconditionSignature(APreconditionSignature node);
    void caseAConstraintException(AConstraintException node);
    void caseAIdentifierList(AIdentifierList node);
    void caseAIdentifierListTail(AIdentifierListTail node);
    void caseAOperationHeader(AOperationHeader node);
    void caseAOperationOperationKeyword(AOperationOperationKeyword node);
    void caseAQueryOperationKeyword(AQueryOperationKeyword node);
    void caseAConstructorOperationKeyword(AConstructorOperationKeyword node);
    void caseAWildcardTypes(AWildcardTypes node);
    void caseAEmptyWildcardTypeTail(AEmptyWildcardTypeTail node);
    void caseAWildcardTypeTail(AWildcardTypeTail node);
    void caseAWildcardType(AWildcardType node);
    void caseAOptionalBehavioralFeatureBody(AOptionalBehavioralFeatureBody node);
    void caseAEmptyOptionalBehavioralFeatureBody(AEmptyOptionalBehavioralFeatureBody node);
    void caseABehavioralFeatureBody(ABehavioralFeatureBody node);
    void caseASimpleInitializationExpression(ASimpleInitializationExpression node);
    void caseAComplexInitializationExpression(AComplexInitializationExpression node);
    void caseASimpleInitialization(ASimpleInitialization node);
    void caseAAttributeDecl(AAttributeDecl node);
    void caseAAttributeInvariant(AAttributeInvariant node);
    void caseAEnumerationLiteralDecl(AEnumerationLiteralDecl node);
    void caseAEnumerationLiteralSlotValues(AEnumerationLiteralSlotValues node);
    void caseAPortDecl(APortDecl node);
    void caseAProvidedPortModifier(AProvidedPortModifier node);
    void caseARequiredPortModifier(ARequiredPortModifier node);
    void caseAPortConnector(APortConnector node);
    void caseAConnectorDecl(AConnectorDecl node);
    void caseAConnectorEndList(AConnectorEndList node);
    void caseAConnectorEndListTail(AConnectorEndListTail node);
    void caseAEmptyConnectorEndListTail(AEmptyConnectorEndListTail node);
    void caseAPathConnectorEnd(APathConnectorEnd node);
    void caseASimpleConnectorEnd(ASimpleConnectorEnd node);
    void caseAInvariantKernel(AInvariantKernel node);
    void caseAPermissionConstraintInvariantKernel(APermissionConstraintInvariantKernel node);
    void caseARegularInvariantConstraint(ARegularInvariantConstraint node);
    void caseAInvariantConstraintKeyword(AInvariantConstraintKeyword node);
    void caseAClassInvariantDecl(AClassInvariantDecl node);
    void caseAReferenceDecl(AReferenceDecl node);
    void caseAOptionalOpposite(AOptionalOpposite node);
    void caseAEmptyOptionalOpposite(AEmptyOptionalOpposite node);
    void caseAAssociationReferenceType(AAssociationReferenceType node);
    void caseACompositionReferenceType(ACompositionReferenceType node);
    void caseAAggregationReferenceType(AAggregationReferenceType node);
    void caseADependencyDecl(ADependencyDecl node);
    void caseAOptionalSubsetting(AOptionalSubsetting node);
    void caseAEmptyOptionalSubsetting(AEmptyOptionalSubsetting node);
    void caseAOptionalQualifier(AOptionalQualifier node);
    void caseAFunctionDecl(AFunctionDecl node);
    void caseATupleType(ATupleType node);
    void caseATupleTypeSlot(ATupleTypeSlot node);
    void caseATupleTypeSlotTail(ATupleTypeSlotTail node);
    void caseAEmptyTupleTypeSlotTail(AEmptyTupleTypeSlotTail node);
    void caseAFunctionSignature(AFunctionSignature node);
    void caseASignature(ASignature node);
    void caseASimpleSignature(ASimpleSignature node);
    void caseAOptionalReturnType(AOptionalReturnType node);
    void caseASimpleOptionalReturnType(ASimpleOptionalReturnType node);
    void caseAParamDeclList(AParamDeclList node);
    void caseAEmptyParamDeclList(AEmptyParamDeclList node);
    void caseASimpleParamDeclList(ASimpleParamDeclList node);
    void caseAEmptySimpleParamDeclList(AEmptySimpleParamDeclList node);
    void caseAParamDeclListTail(AParamDeclListTail node);
    void caseAEmptyParamDeclListTail(AEmptyParamDeclListTail node);
    void caseASimpleParamDeclListTail(ASimpleParamDeclListTail node);
    void caseAEmptySimpleParamDeclListTail(AEmptySimpleParamDeclListTail node);
    void caseAParamDecl(AParamDecl node);
    void caseASimpleParamDecl(ASimpleParamDecl node);
    void caseAOptionalParameterName(AOptionalParameterName node);
    void caseAParameterModifiers(AParameterModifiers node);
    void caseAParameterModifierList(AParameterModifierList node);
    void caseAEmptyParameterModifierList(AEmptyParameterModifierList node);
    void caseAInParameterModifier(AInParameterModifier node);
    void caseAOutParameterModifier(AOutParameterModifier node);
    void caseAInoutParameterModifier(AInoutParameterModifier node);
    void caseAReadParameterModifier(AReadParameterModifier node);
    void caseACreateParameterModifier(ACreateParameterModifier node);
    void caseAUpdateParameterModifier(AUpdateParameterModifier node);
    void caseADeleteParameterModifier(ADeleteParameterModifier node);
    void caseAOptionalRaisesSection(AOptionalRaisesSection node);
    void caseARaisedExceptionList(ARaisedExceptionList node);
    void caseARaisedExceptionListTail(ARaisedExceptionListTail node);
    void caseARaisedExceptionItem(ARaisedExceptionItem node);
    void caseAAnnotations(AAnnotations node);
    void caseAAnnotationsWithBrackets(AAnnotationsWithBrackets node);
    void caseAAnnotationsWithGuillemots(AAnnotationsWithGuillemots node);
    void caseAAnnotationList(AAnnotationList node);
    void caseAAnnotationListTail(AAnnotationListTail node);
    void caseAAnnotation(AAnnotation node);
    void caseAAnnotationOptionalValueSpecs(AAnnotationOptionalValueSpecs node);
    void caseAAnnotationValueSpecList(AAnnotationValueSpecList node);
    void caseAAnnotationValueSpecListTail(AAnnotationValueSpecListTail node);
    void caseAAnnotationValueSpec(AAnnotationValueSpec node);
    void caseAAnnotationValue(AAnnotationValue node);
    void caseAEnumeratedAnnotationValue(AEnumeratedAnnotationValue node);
    void caseAStereotypeDef(AStereotypeDef node);
    void caseAStereotypeDefHeader(AStereotypeDefHeader node);
    void caseAStereotypeExtendsSection(AStereotypeExtendsSection node);
    void caseAEmptyStereotypeExtendsSection(AEmptyStereotypeExtendsSection node);
    void caseAStereotypeExtensionList(AStereotypeExtensionList node);
    void caseAStereotypeExtensionListTail(AStereotypeExtensionListTail node);
    void caseAStereotypeExtension(AStereotypeExtension node);
    void caseAStereotypePropertyDecl(AStereotypePropertyDecl node);
    void caseAOptionalDefault(AOptionalDefault node);
    void caseAEmptyOptionalDefault(AEmptyOptionalDefault node);
    void caseAPrimitiveDef(APrimitiveDef node);
    void caseADetachedOperationDef(ADetachedOperationDef node);
    void caseADetachedOperationHeader(ADetachedOperationHeader node);
    void caseASimpleBlock(ASimpleBlock node);
    void caseAExpressionSimpleBlockResolved(AExpressionSimpleBlockResolved node);
    void caseAStatementSimpleBlockResolved(AStatementSimpleBlockResolved node);
    void caseASimpleExpressionBlock(ASimpleExpressionBlock node);
    void caseASimpleStatementBlock(ASimpleStatementBlock node);
    void caseAWordyBlock(AWordyBlock node);
    void caseAExpressionBlock(AExpressionBlock node);
    void caseABlockKernel(ABlockKernel node);
    void caseAVarDeclSection(AVarDeclSection node);
    void caseAEmptyVarDeclSection(AEmptyVarDeclSection node);
    void caseAVarDecl(AVarDecl node);
    void caseAOptionalType(AOptionalType node);
    void caseAVarListTail(AVarListTail node);
    void caseAStatementSequence(AStatementSequence node);
    void caseAStatement(AStatement node);
    void caseANoIfStatementResolved(ANoIfStatementResolved node);
    void caseAWithIfStatementResolved(AWithIfStatementResolved node);
    void caseABlockNonIfStatement(ABlockNonIfStatement node);
    void caseANonBlockNonIfStatement(ANonBlockNonIfStatement node);
    void caseAWriteClassAttributeSpecificStatement(AWriteClassAttributeSpecificStatement node);
    void caseAWriteAttributeSpecificStatement(AWriteAttributeSpecificStatement node);
    void caseAWriteVariableSpecificStatement(AWriteVariableSpecificStatement node);
    void caseAExpressionSpecificStatement(AExpressionSpecificStatement node);
    void caseAEmptyReturnSpecificStatement(AEmptyReturnSpecificStatement node);
    void caseAValuedReturnSpecificStatement(AValuedReturnSpecificStatement node);
    void caseALoopSpecificStatement(ALoopSpecificStatement node);
    void caseALinkSpecificStatement(ALinkSpecificStatement node);
    void caseAUnlinkSpecificStatement(AUnlinkSpecificStatement node);
    void caseASendSpecificStatement(ASendSpecificStatement node);
    void caseABroadcastSpecificStatement(ABroadcastSpecificStatement node);
    void caseADestroySpecificStatement(ADestroySpecificStatement node);
    void caseARaiseSpecificStatement(ARaiseSpecificStatement node);
    void caseATrySpecificStatement(ATrySpecificStatement node);
    void caseATryStatement(ATryStatement node);
    void caseACatchSection(ACatchSection node);
    void caseAFinallySection(AFinallySection node);
    void caseAIfStatement(AIfStatement node);
    void caseAIfClause(AIfClause node);
    void caseAElseifRestIf(AElseifRestIf node);
    void caseAElseRestIf(AElseRestIf node);
    void caseAEmptyRestIf(AEmptyRestIf node);
    void caseAClauseBody(AClauseBody node);
    void caseAWhileLoopStatement(AWhileLoopStatement node);
    void caseARepeatLoopStatement(ARepeatLoopStatement node);
    void caseALoopTest(ALoopTest node);
    void caseAWhileStatement(AWhileStatement node);
    void caseAWhileLoopBody(AWhileLoopBody node);
    void caseARepeatStatement(ARepeatStatement node);
    void caseARepeatLoopBody(ARepeatLoopBody node);
    void caseAVariableIdentifierExpression(AVariableIdentifierExpression node);
    void caseAClassAttributeIdentifierExpression(AClassAttributeIdentifierExpression node);
    void caseAClassOperationIdentifierExpression(AClassOperationIdentifierExpression node);
    void caseANewIdentifierExpression(ANewIdentifierExpression node);
    void caseASelfIdentifierExpression(ASelfIdentifierExpression node);
    void caseALinkIdentifierExpression(ALinkIdentifierExpression node);
    void caseAAttributeIdentifierExpression(AAttributeIdentifierExpression node);
    void caseAOperationIdentifierExpression(AOperationIdentifierExpression node);
    void caseAExtentIdentifierExpression(AExtentIdentifierExpression node);
    void caseAFunctionIdentifierExpression(AFunctionIdentifierExpression node);
    void caseASimpleAssociationTraversal(ASimpleAssociationTraversal node);
    void caseAQualifiedAssociationTraversal(AQualifiedAssociationTraversal node);
    void caseAVariableAccess(AVariableAccess node);
    void caseALinkRole(ALinkRole node);
    void caseATarget(ATarget node);
    void caseAClosure(AClosure node);
    void caseANamedArgumentList(ANamedArgumentList node);
    void caseAEmptyNamedArgumentList(AEmptyNamedArgumentList node);
    void caseANamedArgument(ANamedArgument node);
    void caseANamedArgumentAdditional(ANamedArgumentAdditional node);
    void caseANamedSimpleValueList(ANamedSimpleValueList node);
    void caseAEmptyNamedSimpleValueList(AEmptyNamedSimpleValueList node);
    void caseANamedSimpleValue(ANamedSimpleValue node);
    void caseANamedSimpleValueAdditional(ANamedSimpleValueAdditional node);
    void caseAExpressionList(AExpressionList node);
    void caseAEmptyExpressionList(AEmptyExpressionList node);
    void caseAExpressionListTail(AExpressionListTail node);
    void caseAExpressionListElement(AExpressionListElement node);
    void caseARootExpression(ARootExpression node);
    void caseAExpression(AExpression node);
    void caseAAlt0ExpressionP0(AAlt0ExpressionP0 node);
    void caseAAlt0ExpressionP1(AAlt0ExpressionP1 node);
    void caseAAlt1ExpressionP1(AAlt1ExpressionP1 node);
    void caseAAlt2ExpressionP1(AAlt2ExpressionP1 node);
    void caseAAlt3ExpressionP1(AAlt3ExpressionP1 node);
    void caseAAlt0ExpressionP2(AAlt0ExpressionP2 node);
    void caseAAlt1ExpressionP2(AAlt1ExpressionP2 node);
    void caseAAlt0ExpressionP3(AAlt0ExpressionP3 node);
    void caseAAlt1ExpressionP3(AAlt1ExpressionP3 node);
    void caseAAlt0ExpressionP4(AAlt0ExpressionP4 node);
    void caseAAlt1ExpressionP4(AAlt1ExpressionP4 node);
    void caseAAlt2ExpressionP4(AAlt2ExpressionP4 node);
    void caseAAlt0ExpressionP5(AAlt0ExpressionP5 node);
    void caseAAlt1ExpressionP5(AAlt1ExpressionP5 node);
    void caseAIsClassExpression(AIsClassExpression node);
    void caseAIdentifierExpressionOperand(AIdentifierExpressionOperand node);
    void caseALiteralOperand(ALiteralOperand node);
    void caseAEmptySetOperand(AEmptySetOperand node);
    void caseATupleOperand(ATupleOperand node);
    void caseAParenthesisOperand(AParenthesisOperand node);
    void caseAIsClassOperand(AIsClassOperand node);
    void caseAClosureOperand(AClosureOperand node);
    void caseAEmptySet(AEmptySet node);
    void caseACast(ACast node);
    void caseAMultMultDiv(AMultMultDiv node);
    void caseADivMultDiv(ADivMultDiv node);
    void caseAPlusAddSub(APlusAddSub node);
    void caseAMinusAddSub(AMinusAddSub node);
    void caseASameComparisonOperator(ASameComparisonOperator node);
    void caseANotSameComparisonOperator(ANotSameComparisonOperator node);
    void caseAEqualsComparisonOperator(AEqualsComparisonOperator node);
    void caseANotEqualsComparisonOperator(ANotEqualsComparisonOperator node);
    void caseALowerThanComparisonOperator(ALowerThanComparisonOperator node);
    void caseAGreaterThanComparisonOperator(AGreaterThanComparisonOperator node);
    void caseAGreaterOrEqualsComparisonOperator(AGreaterOrEqualsComparisonOperator node);
    void caseALowerOrEqualsComparisonOperator(ALowerOrEqualsComparisonOperator node);
    void caseABooleanLiteral(ABooleanLiteral node);
    void caseANumberLiteral(ANumberLiteral node);
    void caseAStringLiteral(AStringLiteral node);
    void caseANullLiteral(ANullLiteral node);
    void caseATupleConstructor(ATupleConstructor node);
    void caseATupleComponentValue(ATupleComponentValue node);
    void caseATupleComponentValueTail(ATupleComponentValueTail node);
    void caseALiteralLiteralOrIdentifier(ALiteralLiteralOrIdentifier node);
    void caseAIdentifierLiteralOrIdentifier(AIdentifierLiteralOrIdentifier node);
    void caseATrueBoolean(ATrueBoolean node);
    void caseAFalseBoolean(AFalseBoolean node);
    void caseAIntegerNumber(AIntegerNumber node);
    void caseARealNumber(ARealNumber node);
    void caseAIntegerMultiplicityValue(AIntegerMultiplicityValue node);
    void caseAInfinityMultiplicityValue(AInfinityMultiplicityValue node);

    void caseTAbstract(TAbstract node);
    void caseTAccess(TAccess node);
    void caseTActor(TActor node);
    void caseTAggregation(TAggregation node);
    void caseTAlias(TAlias node);
    void caseTAllow(TAllow node);
    void caseTAll(TAll node);
    void caseTAnd(TAnd node);
    void caseTAny(TAny node);
    void caseTAnyone(TAnyone node);
    void caseTApply(TApply node);
    void caseTAssociation(TAssociation node);
    void caseTAs(TAs node);
    void caseTAttribute(TAttribute node);
    void caseTBegin(TBegin node);
    void caseTBroadcast(TBroadcast node);
    void caseTBy(TBy node);
    void caseTCall(TCall node);
    void caseTCatch(TCatch node);
    void caseTClazz(TClazz node);
    void caseTComponent(TComponent node);
    void caseTComposition(TComposition node);
    void caseTConnector(TConnector node);
    void caseTConstructor(TConstructor node);
    void caseTCreate(TCreate node);
    void caseTDatatype(TDatatype node);
    void caseTDelete(TDelete node);
    void caseTDeny(TDeny node);
    void caseTDependency(TDependency node);
    void caseTDerived(TDerived node);
    void caseTDestroy(TDestroy node);
    void caseTDo(TDo node);
    void caseTElse(TElse node);
    void caseTElseif(TElseif node);
    void caseTEnd(TEnd node);
    void caseTEntry(TEntry node);
    void caseTEnumeration(TEnumeration node);
    void caseTExit(TExit node);
    void caseTExtends(TExtends node);
    void caseTExtent(TExtent node);
    void caseTExternal(TExternal node);
    void caseTFalse(TFalse node);
    void caseTFinal(TFinal node);
    void caseTFinally(TFinally node);
    void caseTFunction(TFunction node);
    void caseTId(TId node);
    void caseTIf(TIf node);
    void caseTImplements(TImplements node);
    void caseTImport(TImport node);
    void caseTIn(TIn node);
    void caseTInitial(TInitial node);
    void caseTInout(TInout node);
    void caseTInterface(TInterface node);
    void caseTInvariant(TInvariant node);
    void caseTIs(TIs node);
    void caseTLink(TLink node);
    void caseTEnumerationLiteral(TEnumerationLiteral node);
    void caseTLoad(TLoad node);
    void caseTModel(TModel node);
    void caseTNavigable(TNavigable node);
    void caseTNew(TNew node);
    void caseTNone(TNone node);
    void caseTNonunique(TNonunique node);
    void caseTNot(TNot node);
    void caseTNull(TNull node);
    void caseTOn(TOn node);
    void caseTOperation(TOperation node);
    void caseTOpposite(TOpposite node);
    void caseTOr(TOr node);
    void caseTOrdered(TOrdered node);
    void caseTOut(TOut node);
    void caseTPackage(TPackage node);
    void caseTPort(TPort node);
    void caseTPostcondition(TPostcondition node);
    void caseTPrecondition(TPrecondition node);
    void caseTPrimitive(TPrimitive node);
    void caseTPrivate(TPrivate node);
    void caseTProfile(TProfile node);
    void caseTProperty(TProperty node);
    void caseTProtected(TProtected node);
    void caseTProvided(TProvided node);
    void caseTPublic(TPublic node);
    void caseTQuery(TQuery node);
    void caseTRaise(TRaise node);
    void caseTRaises(TRaises node);
    void caseTRead(TRead node);
    void caseTReadonly(TReadonly node);
    void caseTReception(TReception node);
    void caseTReference(TReference node);
    void caseTRepeat(TRepeat node);
    void caseTRequired(TRequired node);
    void caseTReturn(TReturn node);
    void caseTRole(TRole node);
    void caseTSelf(TSelf node);
    void caseTSend(TSend node);
    void caseTSignal(TSignal node);
    void caseTSpecializes(TSpecializes node);
    void caseTState(TState node);
    void caseTStatemachine(TStatemachine node);
    void caseTStatic(TStatic node);
    void caseTStereotype(TStereotype node);
    void caseTSubsets(TSubsets node);
    void caseTTerminate(TTerminate node);
    void caseTThen(TThen node);
    void caseTTo(TTo node);
    void caseTTransition(TTransition node);
    void caseTTrue(TTrue node);
    void caseTTry(TTry node);
    void caseTType(TType node);
    void caseTUnique(TUnique node);
    void caseTUnlink(TUnlink node);
    void caseTUnordered(TUnordered node);
    void caseTUntil(TUntil node);
    void caseTUpdate(TUpdate node);
    void caseTVar(TVar node);
    void caseTWhen(TWhen node);
    void caseTWhere(TWhere node);
    void caseTWhile(TWhile node);
    void caseTPlus(TPlus node);
    void caseTMinus(TMinus node);
    void caseTMult(TMult node);
    void caseTDiv(TDiv node);
    void caseTAssignop(TAssignop node);
    void caseTEquals(TEquals node);
    void caseTEqualsEquals(TEqualsEquals node);
    void caseTNotEqualsEquals(TNotEqualsEquals node);
    void caseTLab(TLab node);
    void caseTLabEquals(TLabEquals node);
    void caseTRab(TRab node);
    void caseTRabEquals(TRabEquals node);
    void caseTNotEquals(TNotEquals node);
    void caseTComma(TComma node);
    void caseTColon(TColon node);
    void caseTSemicolon(TSemicolon node);
    void caseTDot(TDot node);
    void caseTNamespaceSeparator(TNamespaceSeparator node);
    void caseTHash(THash node);
    void caseTLParen(TLParen node);
    void caseTRParen(TRParen node);
    void caseTLBracket(TLBracket node);
    void caseTRBracket(TRBracket node);
    void caseTLCurlyBracket(TLCurlyBracket node);
    void caseTRCurlyBracket(TRCurlyBracket node);
    void caseTRightArrow(TRightArrow node);
    void caseTLeftArrow(TLeftArrow node);
    void caseTLGuillemot(TLGuillemot node);
    void caseTRGuillemot(TRGuillemot node);
    void caseTNotNull(TNotNull node);
    void caseTIdentifier(TIdentifier node);
    void caseTInteger(TInteger node);
    void caseTReal(TReal node);
    void caseTString(TString node);
    void caseTUri(TUri node);
    void caseTComment(TComment node);
    void caseTModelComment(TModelComment node);
    void caseTWhiteSpace(TWhiteSpace node);
    void caseEOF(EOF node);
}
