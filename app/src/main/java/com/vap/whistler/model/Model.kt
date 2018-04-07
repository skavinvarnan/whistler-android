package com.vap.whistler.model

import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson

/**
 * Copyright 2018 (C) Whistler
 * Created on: 02/04/18
 * Author: Kavin Varnan
 */

data class ErrorModel (val code: Int, val message: String)

data class LeaderBoardResponse(val leaderBoard: List<LeaderBoardItem>?, val error: ErrorModel?) {

    class Deserializer : ResponseDeserializable<LeaderBoardResponse> {
        override fun deserialize(content: String) = Gson().fromJson(content, LeaderBoardResponse::class.java)!!
    }

}

data class LeaderBoardItem (val uid: String,
                            val total_for_match: Int, val name: String)


data class ScheduleResponse(val schedule: List<ScheduleItem>?, val error: ErrorModel?) {

    class Deserializer : ResponseDeserializable<ScheduleResponse> {
        override fun deserialize(content: String) = Gson().fromJson(content, ScheduleResponse::class.java)!!
    }

}

data class HappeningMatches(val schedule: List<ScheduleItem>)

data class ScheduleItem (val related_name: String,
                         val displayDate: String,
                         val displayTime: String,
                         val venue: String,
                         val short_name: String,
                         val team_a_name: String,
                         val team_b_name: String,
                         val key: String,
                         val team_a: String,
                         val team_b: String)

data class GenericResponse(val success: Boolean?, val error: ErrorModel?) {

    class Deserializer : ResponseDeserializable<GenericResponse> {
        override fun deserialize(content: String) = Gson().fromJson(content, GenericResponse::class.java)!!
    }

}

data class ScoreBoardResponse(val scoreBoard: ScoreBoard?, val error: ErrorModel?) {

    class Deserializer : ResponseDeserializable<ScoreBoardResponse> {
        override fun deserialize(content: String) = Gson().fromJson(content, ScoreBoardResponse::class.java)!!
    }

}

data class ScoreBoard (
        val teamShortName: String,
        val inningsNumber: String,
        val runsWickets: String,
        val overNumber: String,
        val pShipLabel: String,
        val pShipData: String,
        val crrLabel: String,
        val crrData: String,
        val rrrLabel: String,
        val rrrData: String,
        val matchInfo: String,
        val batsmanNameOne: String,
        val batsmanRunsOne: String,
        val batsmanBallsOne: String,
        val batsman4sOne: String,
        val batsman6sOne: String,
        val batsmanSROne: String,
        val batsmanNameTwo: String,
        val batsmanRunsTwo: String,
        val batsmanBallsTwo: String,
        val batsman4sTwo: String,
        val batsman6sTwo: String,
        val batsmanSRTwo: String,
        val bowlerName: String,
        val bowlerOver: String,
        val bowlerMaiden: String,
        val bowlerRuns: String,
        val bowlerWickets: String,
        val bowlerEconomy: String,
        val title: String,
        val showUpdated: Boolean,
        val battingTeam: String
)

data class PredictPointsResponse(val predictPointsTableData: List<PredictPointsItemArr>?, val teamBatting: String, val error: ErrorModel?) {

    class Deserializer : ResponseDeserializable<PredictPointsResponse> {
        override fun deserialize(content: String) = Gson().fromJson(content, PredictPointsResponse::class.java)!!
    }

}

data class PredictPointsItemArr (val over: PredictPointsItem,
                                 val runs: PredictPointsItem,
                                 val predicted: PredictPointsItem,
                                 val points: PredictPointsItem,
                                 val predictButton: PredictPointsItem)

data class PredictPointsItem (val label: String,
                         val clickable: Boolean,
                         val radius: Int,
                         val colorHex: String,
                         val whiteText: Boolean)

data class MyGroupsResponse(val groups: List<MyGroupItem>?, val error: ErrorModel?) {

    class Deserializer : ResponseDeserializable<MyGroupsResponse> {
        override fun deserialize(content: String) = Gson().fromJson(content, MyGroupsResponse::class.java)!!
    }

}

data class MyGroupItem (val groupId: String,
                        val name: String,
                        val icon: String,
                        val joinCode: String,
                        val admin: String,
                        val members: List<String>,
                        val _id: String)

data class GroupInfoResponse(val groupMembers: List<GroupInfoItem>?, val transfer: TransferItem?, val error: ErrorModel?) {

    class Deserializer : ResponseDeserializable<GroupInfoResponse> {
        override fun deserialize(content: String) = Gson().fromJson(content, GroupInfoResponse::class.java)!!
    }

}

data class GroupInfoItem (val name: String,
                        val uid: String,
                        val over_all_points: Int,
                        val total_for_match: Int)


data class UserPredictionResponse(val userPrediction: List<UserPredictionItem>?, val error: ErrorModel?) {

    class Deserializer : ResponseDeserializable<UserPredictionResponse> {
        override fun deserialize(content: String) = Gson().fromJson(content, UserPredictionResponse::class.java)!!
    }

}

data class UserPredictionItem (val over: String,
                          val runs: String,
                          val predicted: String,
                          val points: String)

data class MatchReportResponse(val allMatches: List<MatchReportItem>?, val error: ErrorModel?) {

    class Deserializer : ResponseDeserializable<MatchReportResponse> {
        override fun deserialize(content: String) = Gson().fromJson(content, MatchReportResponse::class.java)!!
    }

}

data class MatchReportItem (val match: String,
                               val key: String,
                               val points: String)

data class TransferItem (val above_button: String,
                         val main: String,
                         val download: String)

