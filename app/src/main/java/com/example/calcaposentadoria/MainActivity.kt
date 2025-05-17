package com.example.calcaposentadoria

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import android.app.DatePickerDialog


class MainActivity : Activity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        //acessando o spinner sexo
        val spinnerSexo = findViewById<Spinner>(R.id.spinner_sexo)

        //acessando a caixa de data de nascimento
        val editTextDataNascimento = findViewById<EditText>(R.id.edit_text_data_nascimento)

        //acessando a caixa de contribuição
        val editTextContribuicao = findViewById<EditText>(R.id.edit_text_contribuicao)

        //acessando o texto resultado
        val textViewResultado = findViewById<TextView>(R.id.text_view_resultado)

        //acessando o botão de calcular
        val buttonCalcular = findViewById<Button>( R.id.button_calcular)

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("masculino" , "feminino")
        )
        spinnerSexo.adapter =adapter

        var dataNascimentoSelecionada: LocalDate? = null

        editTextDataNascimento.setOnClickListener {
            val hoje = LocalDate.now()
            val dataInicial = dataNascimentoSelecionada ?: hoje  // usa a data salva ou a de hoje

            val datePicker = DatePickerDialog(
                this,
                { _, ano, mes, dia ->
                    dataNascimentoSelecionada = LocalDate.of(ano, mes + 1, dia)  // salva a nova seleção
                    val dataFormatada = String.format("%02d/%02d/%04d", dia, mes + 1, ano)
                    editTextDataNascimento.setText(dataFormatada)
                },
                dataInicial.year,
                dataInicial.monthValue - 1, // DatePicker usa mês de 0 a 11
                dataInicial.dayOfMonth
            )

            datePicker.show()
        }


        buttonCalcular.setOnClickListener {
            val sexoSelecionado = spinnerSexo.selectedItem as String
            val dataNascTexto = editTextDataNascimento.text.toString()
            val contribuicaoTexto = editTextContribuicao.text.toString()

            if (dataNascTexto.isBlank() || contribuicaoTexto.isBlank()) {
                textViewResultado.text = "Informe data de nascimento e anos de contribuição."
                return@setOnClickListener
            }

            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val dataNascimento = try {
                LocalDate.parse(dataNascTexto, formatter)
            } catch (e: Exception) {
                textViewResultado.text = "Data inválida. Use o formato dd/mm/yyyy."
                return@setOnClickListener
            }

            val anosContribuidos = try {
                contribuicaoTexto.toInt()
            } catch (e: Exception) {
                textViewResultado.text = "Número de anos de contribuição inválido."
                return@setOnClickListener
            }

            val hoje = LocalDate.now()
            val idadeAtual = ChronoUnit.YEARS.between(dataNascimento, hoje).toInt()

            val idadeMinima = if (sexoSelecionado == "masculino") 65 else 62
            val tempoContribMin = if (sexoSelecionado == "masculino") 35 else 30

            val faltamIdade = idadeMinima - idadeAtual
            val faltamContribuicao = tempoContribMin - anosContribuidos

            val criterio = when {
                faltamIdade <= 0 && faltamContribuicao <= 0 ->
                    "Você já pode se aposentar por idade e por tempo de contribuição!"

                faltamIdade <= 0 ->
                    "Você já pode se aposentar por idade, mas falta $faltamContribuicao anos de contribuição."

                faltamContribuicao <= 0 ->
                    "Você já pode se aposentar por tempo de contribuição, mas faltam $faltamIdade anos de idade."

                else -> {
                    if (faltamIdade <= faltamContribuicao) {
                        "Você se aposentará primeiro por idade. Faltam $faltamIdade anos."
                    } else {
                        "Você se aposentará primeiro por tempo de contribuição. Faltam $faltamContribuicao anos."
                    }
                }
            }

            textViewResultado.text = criterio
        }
    }

}