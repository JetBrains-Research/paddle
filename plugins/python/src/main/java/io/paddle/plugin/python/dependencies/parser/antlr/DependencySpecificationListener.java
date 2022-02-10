package io.paddle.plugin.python.dependencies.parser.antlr;

// Generated from DependencySpecification.g4 by ANTLR 4.8

import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link DependencySpecificationParser}.
 */
public interface DependencySpecificationListener extends ParseTreeListener {
    /**
     * Enter a parse tree produced by {@link DependencySpecificationParser#version}.
     *
     * @param ctx the parse tree
     */
    void enterVersion(DependencySpecificationParser.VersionContext ctx);

    /**
     * Exit a parse tree produced by {@link DependencySpecificationParser#version}.
     *
     * @param ctx the parse tree
     */
    void exitVersion(DependencySpecificationParser.VersionContext ctx);

    /**
     * Enter a parse tree produced by {@link DependencySpecificationParser#versionCmp}.
     *
     * @param ctx the parse tree
     */
    void enterVersionCmp(DependencySpecificationParser.VersionCmpContext ctx);

    /**
     * Exit a parse tree produced by {@link DependencySpecificationParser#versionCmp}.
     *
     * @param ctx the parse tree
     */
    void exitVersionCmp(DependencySpecificationParser.VersionCmpContext ctx);

    /**
     * Enter a parse tree produced by {@link DependencySpecificationParser#versionOne}.
     *
     * @param ctx the parse tree
     */
    void enterVersionOne(DependencySpecificationParser.VersionOneContext ctx);

    /**
     * Exit a parse tree produced by {@link DependencySpecificationParser#versionOne}.
     *
     * @param ctx the parse tree
     */
    void exitVersionOne(DependencySpecificationParser.VersionOneContext ctx);

    /**
     * Enter a parse tree produced by {@link DependencySpecificationParser#versionMany}.
     *
     * @param ctx the parse tree
     */
    void enterVersionMany(DependencySpecificationParser.VersionManyContext ctx);

    /**
     * Exit a parse tree produced by {@link DependencySpecificationParser#versionMany}.
     *
     * @param ctx the parse tree
     */
    void exitVersionMany(DependencySpecificationParser.VersionManyContext ctx);

    /**
     * Enter a parse tree produced by {@link DependencySpecificationParser#versionspec}.
     *
     * @param ctx the parse tree
     */
    void enterVersionspec(DependencySpecificationParser.VersionspecContext ctx);

    /**
     * Exit a parse tree produced by {@link DependencySpecificationParser#versionspec}.
     *
     * @param ctx the parse tree
     */
    void exitVersionspec(DependencySpecificationParser.VersionspecContext ctx);

    /**
     * Enter a parse tree produced by {@link DependencySpecificationParser#markerOp}.
     *
     * @param ctx the parse tree
     */
    void enterMarkerOp(DependencySpecificationParser.MarkerOpContext ctx);

    /**
     * Exit a parse tree produced by {@link DependencySpecificationParser#markerOp}.
     *
     * @param ctx the parse tree
     */
    void exitMarkerOp(DependencySpecificationParser.MarkerOpContext ctx);

    /**
     * Enter a parse tree produced by {@link DependencySpecificationParser#markerVar}.
     *
     * @param ctx the parse tree
     */
    void enterMarkerVar(DependencySpecificationParser.MarkerVarContext ctx);

    /**
     * Exit a parse tree produced by {@link DependencySpecificationParser#markerVar}.
     *
     * @param ctx the parse tree
     */
    void exitMarkerVar(DependencySpecificationParser.MarkerVarContext ctx);

    /**
     * Enter a parse tree produced by {@link DependencySpecificationParser#markerExpr}.
     *
     * @param ctx the parse tree
     */
    void enterMarkerExpr(DependencySpecificationParser.MarkerExprContext ctx);

    /**
     * Exit a parse tree produced by {@link DependencySpecificationParser#markerExpr}.
     *
     * @param ctx the parse tree
     */
    void exitMarkerExpr(DependencySpecificationParser.MarkerExprContext ctx);

    /**
     * Enter a parse tree produced by {@link DependencySpecificationParser#markerAnd}.
     *
     * @param ctx the parse tree
     */
    void enterMarkerAnd(DependencySpecificationParser.MarkerAndContext ctx);

    /**
     * Exit a parse tree produced by {@link DependencySpecificationParser#markerAnd}.
     *
     * @param ctx the parse tree
     */
    void exitMarkerAnd(DependencySpecificationParser.MarkerAndContext ctx);

    /**
     * Enter a parse tree produced by {@link DependencySpecificationParser#markerOr}.
     *
     * @param ctx the parse tree
     */
    void enterMarkerOr(DependencySpecificationParser.MarkerOrContext ctx);

    /**
     * Exit a parse tree produced by {@link DependencySpecificationParser#markerOr}.
     *
     * @param ctx the parse tree
     */
    void exitMarkerOr(DependencySpecificationParser.MarkerOrContext ctx);

    /**
     * Enter a parse tree produced by {@link DependencySpecificationParser#marker}.
     *
     * @param ctx the parse tree
     */
    void enterMarker(DependencySpecificationParser.MarkerContext ctx);

    /**
     * Exit a parse tree produced by {@link DependencySpecificationParser#marker}.
     *
     * @param ctx the parse tree
     */
    void exitMarker(DependencySpecificationParser.MarkerContext ctx);

    /**
     * Enter a parse tree produced by {@link DependencySpecificationParser#quotedMarker}.
     *
     * @param ctx the parse tree
     */
    void enterQuotedMarker(DependencySpecificationParser.QuotedMarkerContext ctx);

    /**
     * Exit a parse tree produced by {@link DependencySpecificationParser#quotedMarker}.
     *
     * @param ctx the parse tree
     */
    void exitQuotedMarker(DependencySpecificationParser.QuotedMarkerContext ctx);

    /**
     * Enter a parse tree produced by {@link DependencySpecificationParser#name}.
     *
     * @param ctx the parse tree
     */
    void enterName(DependencySpecificationParser.NameContext ctx);

    /**
     * Exit a parse tree produced by {@link DependencySpecificationParser#name}.
     *
     * @param ctx the parse tree
     */
    void exitName(DependencySpecificationParser.NameContext ctx);

    /**
     * Enter a parse tree produced by {@link DependencySpecificationParser#extrasList}.
     *
     * @param ctx the parse tree
     */
    void enterExtrasList(DependencySpecificationParser.ExtrasListContext ctx);

    /**
     * Exit a parse tree produced by {@link DependencySpecificationParser#extrasList}.
     *
     * @param ctx the parse tree
     */
    void exitExtrasList(DependencySpecificationParser.ExtrasListContext ctx);

    /**
     * Enter a parse tree produced by {@link DependencySpecificationParser#extras}.
     *
     * @param ctx the parse tree
     */
    void enterExtras(DependencySpecificationParser.ExtrasContext ctx);

    /**
     * Exit a parse tree produced by {@link DependencySpecificationParser#extras}.
     *
     * @param ctx the parse tree
     */
    void exitExtras(DependencySpecificationParser.ExtrasContext ctx);

    /**
     * Enter a parse tree produced by {@link DependencySpecificationParser#nameReq}.
     *
     * @param ctx the parse tree
     */
    void enterNameReq(DependencySpecificationParser.NameReqContext ctx);

    /**
     * Exit a parse tree produced by {@link DependencySpecificationParser#nameReq}.
     *
     * @param ctx the parse tree
     */
    void exitNameReq(DependencySpecificationParser.NameReqContext ctx);

    /**
     * Enter a parse tree produced by {@link DependencySpecificationParser#specification}.
     *
     * @param ctx the parse tree
     */
    void enterSpecification(DependencySpecificationParser.SpecificationContext ctx);

    /**
     * Exit a parse tree produced by {@link DependencySpecificationParser#specification}.
     *
     * @param ctx the parse tree
     */
    void exitSpecification(DependencySpecificationParser.SpecificationContext ctx);
}
